# Assumptions

The specification leaves several points open. This document lists the interpretations and design decisions made so far.

## Database

- **PostgreSQL** is used as the database, a popular choice for persistence.
- Only the database runs in Docker; the application itself is run with Maven.

## Schema management
 
- **Flyway owns the schema.** So Hibernate runs with `spring.jpa.hibernate.ddl-auto=validate`: it never creates or alters tables, it only checks that the entities match the migrations.
- **Migration naming is validated** I set up `spring.flyway.validate-migration-naming=true`, so a misnamed migration file makes startup fail instead of being silently ignored (I had the case of a file name that I incorrectly typed: v1__init.sql instead of V1_init.sql)
- **Open Session in View is disabled** (`spring.jpa.open-in-view=false`). When true, it keeps a JPA session, and therefore a database connection, open for the whole HTTP request so that lazy associations can be loaded. The pool has 10 connections by default, so under load it is exhausted by requests that are no longer doing any database work, and every other request waits. With it disabled, a connection is held only for the service transaction, and lazy associations must be loaded by the service before it returns, never by the controller.

## The state machine

- **The state machine is implemented without a dedicated library.** the diagram is small and fixed, so a library would bring configuration and dependencies without adding capability.
- **The state machine is a plain Java class, not a Spring bean.** It is pure domain logic, with no dependencies.

## Package structure

- Two top-level packages mirror the inside and the outside of the hexagon: `core` holds `domain` (entity, states, events, state machine, business exceptions) and `usecases` (services, commands and the repository port); `infrastructure` holds `api` (REST adapter) and `persistence` (JPA adapter). The dependency rule is that `infrastructure` depends on `core` and never the reverse; inside `core`, `usecases` depends on `domain`, which depends on nothing else.
- This is a simplified form of the hexagonal layout described by Tom Hombergs (*Get Your Hands Dirty on Clean Architecture*). Feature-level packages, input ports (one interface per use case) and a separate persistence model are deliberately left out: with a single aggregate and about a dozen classes they would add indirection without protecting anything. The one element kept is the output port, `RequestRepository`, declared in `usecases` and implemented in `persistence`, so the use cases never depend on Spring Data.
- The adapters are not split into driving (`in`) and driven (`out`) sub-packages, because there is exactly one adapter on each side and the names `api` and `persistence` already say which side they are on. The first additional adapter (a scheduler, a message listener, an external client) would introduce that split.

## Domain model

- **Create is not a transition.** It starts from the initial pseudo-state of the diagram, so it is a factory method, `Request.create(name, content)`, that always produces a request in `CREATED`.
- **PUBLISHED is a named state.** The diagram shows an unnamed final node after `Publish`; since the specification attaches a publication number to published requests, it is modeled as an explicit state.
- **The state can only change through the event methods of `Request`** (`verify`, `accept`, `reject`, `delete`, `publish`), each checked against the state machine. There is no setter for the state, so a request can never reach a state it did not get to through a valid transition, including in tests.
- **Reasons.** A reason is mandatory for `reject` and `delete`. Null and blank (whitespace only) are treated as missing, and the check happens before the transition, so a missing reason leaves the state unchanged. The reason is trimmed and stored on the request.
- **Deletion is logical.** A deleted request is not removed; it moves to the terminal `DELETED` state with its reason. Physically removing it would make the mandatory reason pointless and break the audit history.
- **Editing is not a transition.** `updateContent` replaces the body without changing the state, and is refused outside the states flagged as editable (`CREATED`, `VERIFIED`). Only the body is editable; the name is immutable after creation.

## Use cases

- **One `RequestService` with one method per use case** (`create`, `updateContent`, `verify`, `accept`, `reject`, `delete`, `publish`, `get`), rather than one class per use case as often prescribed in hexagonal architecture. The methods are thin (load by id, call the domain object, save) and share the same transaction pattern; separate classes would be ceremony at this size.
- **Business rules are enforced by the domain, not the service.** The service only orchestrates and lets domain exceptions propagate. It adds `RequestNotFoundException` for unknown ids, which is a lookup concern and therefore lives in `usecases`, not `domain`. 
- **Service tests use the real domain objects and a mocked repository port.** The domain is pure and fast, so mocking it would only hide bugs.

## Persistence

- **The domain entity carries the JPA mapping annotations.** A separate persistence model with a mapper would keep the domain fully free of persistence metadata, but for a flat aggregate it would duplicate the fields and require a way to rebuild a request in any state, bypassing the state machine. This is a conscious choice for a small application.
- **The repository port is implemented by Spring Data.** `JpaRequestRepository` extends both `JpaRepository` and the `RequestRepository` port, so Spring Data generates the implementation and the only hand-written persistence code is the native query drawing the publication number from the sequence.
- **The state is stored by name** (`@Enumerated(EnumType.STRING)`), not by ordinal, so reordering or inserting enum constants can never corrupt stored data. A test guards this.
- **Optimistic locking with `@Version`.** Two transactions modifying the same request from the same snapshot cannot both succeed; the second one fails with `ObjectOptimisticLockingFailureException` and is rolled back.
- **Deletion keeps the row.** Consistent with the logical deletion decided in the domain, there is no `DELETE` statement; a deleted request is a row in the `DELETED` state.
- Ids are database-generated (`IDENTITY`); the publication number is a separate column with a unique constraint.

## API

- **Transitions are action endpoints** (`POST /requests/{id}/verify`, `/accept`, `/reject`, `/delete`, `/publish`) rather than a generic update of a `state` field. Each action maps to one use case, carries only the input it needs and makes invalid requests impossible to express.
- **Deletion is `POST /requests/{id}/delete`, not HTTP `DELETE`.** It is a state transition that requires a body (the reason) and leaves the resource readable afterwards, whereas HTTP `DELETE` implies removal and discourages request bodies.
- **Mutations return as little as possible.** Transitions and content updates answer 204 with no body; `create` answers 201 with a `Location` header and the id; `publish` returns the publication number, as it is the only information the caller cannot know in advance. The full representation is available from `GET /requests/{id}`. This follows the recommendation to return the minimum from a use case, at the cost of one extra call for clients wanting the updated state immediately.
- **Input validation happens at the boundary.** Commands carry Bean Validation constraints and are validated by `@Valid` in the controller, so syntactic errors (blank name, missing reason) are rejected with 400 before any use case runs. Business rules are still enforced by the domain and surface as 409.
- **All errors are RFC 9457 problem details**, including the ones produced by Spring itself (validation, malformed JSON, unknown routes), through `ResponseEntityExceptionHandler`.
- **Status mapping:** 404 for an unknown id; 409 for a transition the current state does not allow, an edit outside the editable states, and an optimistic locking conflict (the client reloads and retries); 400 for validation errors; 500 for anything unexpected.
- **Messages from domain exceptions are returned to the client as-is.** They are written for the end user by contract (see `DomainException` and `RequestNotFoundException`) and contain only information the caller already has. Messages from frameworks (Spring, Hibernate) are never exposed: the optimistic locking and the unexpected-error responses carry a fixed sentence, and the unexpected error is logged server-side with its stack trace.

## Audit Log
- **The history is part of the `Request`.** Each state change appends a `StateChange` entry to the request's history.
- **Creation is the first entry**, with `CREATE` as event and no previous state.
- **Refused transitions are not recorded.** The history reflects what happened to the request, not what was attempted.
- **Content edits are not audited.** The specification asks for the history of state changes, and editing the body does not change the state.
- **The history is returned in full, oldest first.** The diagram has no cycles, so a request has at most four entries (creation plus three transitions on the longest path); pagination would be pointless.

## Dependencies

- The IDE's dependency scanner was run on the final dependency tree. Four libraries are pinned above the versions managed by Spring Boot 4.1.1 to pick up security fixes published in October 2026; the overrides are grouped and commented in the pom.xml and should be removed once Spring Boot manages patched versions.

## Testing

- **The project is developed test-first (TDD).** Each behavior starts with a failing test, followed by the minimal implementation and a refactoring step.
- Integration tests run against a real PostgreSQL through Testcontainers rather than an in-memory database such as H2, so they exercise the same SQL, sequences and constraints as the real database.
- The test container uses the same PostgreSQL version as the Compose database.
- **Test pyramid.** The domain and the use cases are covered by fast, pure unit tests (the domain with no mocks at all, the use cases with a mocked repository port). The adapters are covered by Spring slice tests, `@DataJpaTest` against a real PostgreSQL in Testcontainers for persistence and `@WebMvcTest` with a mocked service for the API. A few end-to-end scenarios go through the full stack over MockMvc to prove the wiring; they do not repeat rules already tested below.
