# Assumptions

The specification leaves several points open. This document lists the interpretations and design decisions made so far.

## Database

- **PostgreSQL** is used as the database, a popular choice for persistence.
- Only the database runs in Docker; the application itself is run with Maven.

## Schema management
 
- **Flyway owns the schema.** So Hibernate runs with `spring.jpa.hibernate.ddl-auto=validate`: it never creates or alters tables, it only checks that the entities match the migrations.
- **Migration naming is validated** I set up `spring.flyway.validate-migration-naming=true`, so a misnamed migration file makes startup fail instead of being silently ignored (I had the case of a file name that I incorrectly typed: v1__init.sql instead of V1_init.sql)
- `spring.jpa.open-in-view=false`: the database session is closed at the end of each service transaction instead of being held for the whole HTTP request. This avoids holding connections during serialization and hidden lazy-loading queries; data needed by a response is loaded explicitly in the service layer.

## The state machine

- **The state machine is implemented without a dedicated library.** the diagram is small and fixed, so a library would bring configuration and dependencies without adding capability.
- **The state machine is a plain Java class, not a Spring bean.** It is pure domain logic, with no dependencies.

## Package structure

- Four flat packages: `domain` (entity, states, events, state machine, business exceptions), `usecases` (services and the repository port), `persistence` (JPA adapter) and `web` (REST adapter). Dependencies point inward: `web` and `persistence` depend on `usecases` and `domain`, `usecases` depends on `domain`, `domain` depends on nothing else.
- This is a simplified form of the hexagonal layout described by Tom Hombergs (*Get Your Hands Dirty on Clean Architecture*).

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

## Testing

- **The project is developed test-first (TDD).** Each behavior starts with a failing test, followed by the minimal implementation and a refactoring step.
- Integration tests run against a real PostgreSQL through Testcontainers rather than an in-memory database such as H2, so they exercise the same SQL, sequences and constraints as the real database.
- The test container uses the same PostgreSQL version as the Compose database.

