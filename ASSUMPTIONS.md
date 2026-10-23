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

## Testing

- **The project is developed test-first (TDD).** Each behavior starts with a failing test, followed by the minimal implementation and a refactoring step.
- Integration tests run against a real PostgreSQL through Testcontainers rather than an in-memory database such as H2, so they exercise the same SQL, sequences and constraints as the real database.
- The test container uses the same PostgreSQL version as the Compose database.

