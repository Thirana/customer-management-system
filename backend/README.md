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
- One-line request completion logs with method, path, status, duration, and request ID
- Lightweight service logs for customer create, update, delete, and expected business rejections
- Importable Postman collection and local environment under `../docs/postman/`
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

Each completed HTTP request is logged once with method, path, response status, and duration:

```text
HTTP request completed method=GET path=/api/v1/customers status=200 durationMs=34
```

The log line includes the request ID through the Logback pattern. Request bodies and query strings are not logged because they can contain personal data.

`CustomerService` also logs successful create, update, and delete operations with `customerId`, plus warning logs for expected business rejections such as duplicate NIC checks and invalid linked IDs.

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

## Postman Artifacts

Import these two files from the repository root:

```text
docs/postman/customer-management-api.postman_collection.json
docs/postman/customer-management-local.postman_environment.json
```

Select the `Customer Management Local` environment before sending requests.

The environment parameterizes:

- `baseUrl`
- `customerId`
- `missingCustomerId`
- `page`, `size`, `sortBy`, and `sortDir`
- create/update scalar fields
- `customerMobileNumbers` and `updatedCustomerMobileNumbers` as raw JSON arrays
- `customerAddresses` and `updatedCustomerAddresses` as raw JSON arrays of address objects
- `customerFamilyMemberIds` and `updatedCustomerFamilyMemberIds` as raw JSON arrays

The create and update requests use raw JSON variables for collection fields, so you can test multiple mobile numbers, addresses, and family member IDs without editing the request body structure.

Suggested flow:

1. Start MariaDB.
2. Run `backend/scripts/seed-dev-customers.sh`.
3. Start the backend.
4. Send `List Customers`.
5. Copy an `id` from the response into the `customerId` environment variable.
6. Send detail, update, and delete requests as needed.

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
