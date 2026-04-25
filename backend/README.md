# Customer Management Backend

Spring Boot REST API for the Customer Management System interview assignment. The backend manages customer records, master data lookups, validation, persistence, request tracing, and API documentation.

## Stack

- Java 8 target
- Spring Boot 2.7.18
- Maven
- Spring Web
- Spring Data JPA
- Bean Validation
- Flyway
- MariaDB for local development
- H2 for repository and context tests
- Lombok
- springdoc-openapi Swagger UI
- JUnit 5

## Current Baseline

- Versioned API prefix: `/api/v1`
- Customer CRUD endpoints
- City lookup endpoint for address dropdowns
- Flyway migrations for master data and customer tables
- Seeded country and city master data
- JPA entities with lazy relationships by default
- Summary projection for customer list pagination
- Entity graph query for customer detail loading
- Consistent `ApiResponse<T>` and `PageResponse<T>` envelopes
- Global exception handling for expected API failures and validation errors
- Request ID support with `X-Request-ID`
- Dev seed script for customer records
- Backend tests for repositories, services, controllers, filters, exceptions, and app context

Bulk Excel import is planned for the next backend phase.

## Profiles and Configuration

The default active profile is `dev`.

Important config files:

```text
src/main/resources/application.yml
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml
src/main/resources/logback-spring.xml
```

The `dev` profile expects the root Docker Compose MariaDB service:

```text
Host: localhost
Port: 3306
Database: customer_management
Username: cms_user
Password: cms_password
```

Custom application settings are validated through `AppProperties`. Invalid settings should fail application startup.

## Database and Migrations

Flyway migration scripts live under:

```text
src/main/resources/db/migration/
```

Current migrations:

```text
V1__create_master_data_tables.sql
V2__create_customer_tables.sql
V3__add_indexes.sql
V4__seed_master_data.sql
```

Hibernate uses `ddl-auto: validate` for real database profiles. Schema changes should be made through Flyway migrations, not Hibernate auto-DDL.

## Run Locally

From the repository root, start MariaDB:

```bash
docker compose up -d mariadb
```

From `backend/`, start the API:

```bash
mvn spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## API Endpoints

Customer endpoints:

```text
GET    /api/v1/customers
GET    /api/v1/customers/{id}
POST   /api/v1/customers
PUT    /api/v1/customers/{id}
DELETE /api/v1/customers/{id}
```

Customer list query parameters:

```text
page=0
size=10
sortBy=name | nicNumber | dateOfBirth | id
sortDir=asc | desc
```

Master data endpoint:

```text
GET /api/v1/cities
```

All endpoints return an `ApiResponse<T>` envelope. List endpoints return `PageResponse<T>` inside that envelope.

## Request ID Behavior

Every request receives an `X-Request-ID` response header.

If the client sends `X-Request-ID`, the backend uses that value. Otherwise the request filter generates a UUID. The value is stored in MDC so request-scoped logs can be correlated.

## Dev Customer Seed

From the repository root:

```bash
backend/scripts/seed-dev-customers.sh
```

The script:

- deletes only customers with NIC values matching `DEV-NIC-*`
- inserts 8 deterministic customers
- inserts related mobile numbers and addresses
- inserts a few family member links
- is safe to rerun

Use this before Postman testing when you want predictable data for GET endpoints.

## Test

Run all backend tests from `backend/`:

```bash
mvn test
```

The current test suite covers:

- context startup
- request ID filter behavior
- global exception response mapping
- repository lookups and custom queries
- customer service create/update/delete rules
- controller validation, status codes, and response envelopes

## Useful Commands

From the repository root:

```bash
docker compose up -d mariadb
docker compose ps
docker compose down
backend/scripts/seed-dev-customers.sh
```

From `backend/`:

```bash
mvn test
mvn spring-boot:run
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Troubleshooting

- If startup fails with a database connection error, confirm the MariaDB container is healthy with `docker compose ps`.
- If startup fails with Flyway validation errors, check whether migration files were modified after being applied to the local database.
- If startup fails with Hibernate validation errors, entity mappings no longer match the Flyway schema.
- If port `8080` is already used, stop the existing process before running the backend.
- If Swagger is not available, verify the backend started and open `http://localhost:8080/swagger-ui.html`.
