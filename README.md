# Request Management Application

# Request Management
 
Backend service managing the lifecycle of requests, where each request moves through a defined set of states governed by a state diagram. The functionality will be exposed as a REST API; there is no GUI.
 
Design decisions are documented in [ASSUMPTIONS.md](ASSUMPTIONS.md).
 
## Tech stack
 
- Java and Spring Boot 4 (Web, Data JPA, Validation)
- PostgreSQL 18, schema managed with Flyway
- JUnit 5, AssertJ and Testcontainers for tests
- Maven (wrapper included)

## Prerequisites
 
- A JDK matching the `java.version` declared in `pom.xml`
- Docker, used both by Docker Compose (local database) and by Testcontainers (tests)

## Running the application
 
Start the database, then the application:
 
```bash
docker compose up -d
./mvnw spring-boot:run
```
 
Flyway applies the migrations on startup.
 
To stop the database (add `-v` to also delete its data):
 
```bash
docker compose down
```
 
## Running the tests
 
```bash
./mvnw clean test
```
 
Integration tests start their own PostgreSQL container through Testcontainers, so Docker must be running, but the Compose database is not needed.

## API

| Method | Path                           | Body                  | Response                                      |
|--------|--------------------------------|-----------------------|-----------------------------------------------|
| POST   | `/requests`                    | `{"name", "content"}` | 201, `Location: /requests/{id}`, `{"id"}`     |
| GET    | `/requests/{id}`               |                       | 200, full request                             |
| PUT    | `/requests/{id}/content`       | `{"content"}`         | 204 (CREATED or VERIFIED only)                |
| POST   | `/requests/{id}/verify`        |                       | 204, CREATED to VERIFIED                      |
| POST   | `/requests/{id}/accept`        |                       | 204, VERIFIED to ACCEPTED                     |
| POST   | `/requests/{id}/reject`        | `{"reason"}`          | 204, VERIFIED or ACCEPTED to REJECTED         |
| POST   | `/requests/{id}/delete`        | `{"reason"}`          | 204, CREATED to DELETED                       |
| POST   | `/requests/{id}/publish`       |                       | 200, `{"publicationNumber"}`, ACCEPTED to PUBLISHED |

Mutations return no representation; `GET /requests/{id}` returns the current state:

```json
{
  "id": 1,
  "name": "My request",
  "content": "Some content",
  "state": "VERIFIED",
  "reason": null,
  "publicationNumber": null
}
```

Examples:

```bash
curl -i -X POST localhost:8080/requests \
  -H 'Content-Type: application/json' \
  -d '{"name": "My request", "content": "Some content"}'
 
curl -i -X POST localhost:8080/requests/1/verify
 
curl -i -X POST localhost:8080/requests/1/reject \
  -H 'Content-Type: application/json' \
  -d '{"reason": "Incomplete content"}'
 
curl localhost:8080/requests/1
```

### Errors

Errors are returned as RFC 9457 problem details (`application/problem+json`):

| Status | When                                                                                   |
|--------|----------------------------------------------------------------------------------------|
| 400    | Validation failure: missing or blank name, content or reason; malformed JSON           |
| 404    | No request with that id                                                                |
| 409    | Transition not allowed from the current state, content not editable, or concurrent modification |
| 500    | Unexpected error; the response carries no technical detail, the server log does        |

## Project structure

```
src/main/java/io/github/alexandrefilipiak/requestmanagement
    core
        domain            Request entity, states, events, state machine, business exceptions
        usecases          RequestService, commands and the RequestRepository port
    infrastructure
        api               REST controller, response records, exception handler
        persistence       Spring Data JPA implementation of the port
src/main/resources/db/migration     Flyway migrations
src/test/java/...                   Tests and Testcontainers configuration
compose.yaml                        Local PostgreSQL
```