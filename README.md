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
 
## Project structure
 
```
# Main classes
src/main/java/io/github/alexandrefilipiak/requestmanagement

# Main 
src/main/resources/application.properties

# Flyway migrations
src/main/resources/db/migration

# Tests and Testcontainers configuration
src/test/...

# Local PostgreSQL
compose.yaml
```
