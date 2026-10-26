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

The same command produces a JaCoCo coverage report in `target/site/jacoco/index.html`. Coverage is used to find untested paths

## API

| Method | Path                           | Body                  | Response                                      |
|--------|--------------------------------|-----------------------|-----------------------------------------------|
| POST   | `/requests`                    | `{"name", "content"}` | 201, `Location: /requests/{id}`, `{"id"}`     |
| GET    | `/requests`                    |                       | 200, page of requests (see parameters below)  |
| GET    | `/requests/{id}`               |                       | 200, full request                             |
| PUT    | `/requests/{id}/content`       | `{"content"}`         | 204 (CREATED or VERIFIED only)                |
| POST   | `/requests/{id}/verify`        |                       | 204, CREATED to VERIFIED                      |
| POST   | `/requests/{id}/accept`        |                       | 204, VERIFIED to ACCEPTED                     |
| POST   | `/requests/{id}/reject`        | `{"reason"}`          | 204, VERIFIED or ACCEPTED to REJECTED         |
| POST   | `/requests/{id}/delete`        | `{"reason"}`          | 204, CREATED to DELETED                       |
| POST   | `/requests/{id}/publish`       |                       | 200, `{"publicationNumber"}`, ACCEPTED to PUBLISHED |
| GET    | `/requests/{id}/history`       |                       | 200, state changes oldest first                     |

Listing parameters: `name` (partial match, case-insensitive), `state` (exact), `page` (0-based, default 0), `size` (default 10, maximum 100). Filters combine with AND; results are newest first.

```json
{
  "items": [ { "id": 3, "name": "Budget approval", "state": "VERIFIED", "...": "..." } ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

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

The history lists every state change, including the creation, oldest first:

```json
{
  "requestId": 1,
  "entries": [
    {"fromState": null, "toState": "CREATED", "event": "CREATE", "reason": null, "occurredAt": "2026-10-25T20:14:03.120Z"},
    {"fromState": "CREATED", "toState": "VERIFIED", "event": "VERIFY", "reason": null, "occurredAt": "2026-10-25T20:15:41.887Z"},
    {"fromState": "VERIFIED", "toState": "REJECTED", "event": "REJECT", "reason": "Incomplete content", "occurredAt": "2026-10-25T20:16:12.004Z"}
  ]
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
curl localhost:8080/requests/1/history
curl 'localhost:8080/requests?name=budget&state=VERIFIED&page=0&size=10'
```

### Errors

Errors are returned as RFC 9457 problem details (`application/problem+json`):

| Status | When                                                                                   |
|--------|----------------------------------------------------------------------------------------|
| 400    | Validation failure: missing or blank name, content or reason; malformed JSON; invalid listing parameters |
| 404    | No request with that id                                                                |
| 409    | Transition not allowed from the current state, content not editable, or concurrent modification |
| 500    | Unexpected error; the response carries no technical detail, the server log does        |

## Project structure

```
src/main/java/io/github/alexandrefilipiak/requestmanagement
    core
        domain            Request and StateChange entities, states, events, state machine, business exceptions
        usecases          RequestService, commands and the RequestRepository port
    infrastructure
        api               REST controller, response records, exception handler
        persistence       Spring Data JPA implementation of the port
src/main/resources/db/migration     Flyway migrations
src/test/java/...                   Tests and Testcontainers configuration
compose.yaml                        Local PostgreSQL
```
 