# Customer Management System

Full-stack customer management system for a software engineer interview assignment. The repository is structured as a monorepo with a Spring Boot backend, a planned React frontend, Docker-managed MariaDB, Flyway migrations, Swagger API documentation, and backend automated tests.

## Current Baseline

- Monorepo with backend Maven project under `backend/`
- Docker Compose MariaDB 10.6 for local development
- Flyway migration-only schema workflow
- Spring Boot 2.7.18 backend targeting Java 8
- Customer domain model with mobile numbers, addresses, cities, countries, and family members
- Customer CRUD API under `/api/v1/customers`
- City lookup API under `/api/v1/cities`
- Consistent `ApiResponse<T>` and `PageResponse<T>` response envelopes
- Request ID support through `X-Request-ID` response header and MDC logging
- One-line request completion logs with method, path, status, duration, and request ID
- Lightweight service logs for customer create, update, delete, and expected business rejections
- Swagger UI through springdoc-openapi
- Importable Postman collection and local environment
- Dev customer seed script for Postman and manual GET endpoint testing
- Backend unit, MVC slice, repository, and context tests

The frontend is planned but has not been scaffolded yet.

## Repository Structure

```text
customer-management-system/
├── backend/                 Spring Boot API project
│   ├── pom.xml
│   ├── README.md
│   ├── scripts/
│   └── src/
├── docs/postman/            Importable Postman collection and environment
├── docker-compose.yml       Local MariaDB service
├── README.md                Full monorepo setup and reviewer entrypoint
└── doc/                     Local planning docs, ignored by Git
```

## Prerequisites

- Java 8 compatible target support
- Maven
- Docker Desktop or another running Docker daemon
- Postman or another API client for manual testing

The backend currently runs locally on Java 21 as well, while Maven compiles the project with Java 8 target compatibility.

## Local Setup

1. Start MariaDB from the repository root.

```bash
docker compose up -d mariadb
```

2. Confirm the database container is healthy.

```bash
docker compose ps
```

3. Start the backend.

```bash
cd backend
mvn spring-boot:run
```

The default active profile is `dev`. Flyway runs automatically on startup and applies the schema plus master data.

4. Open Swagger.

```text
http://localhost:8080/swagger-ui.html
```

## Database

Local MariaDB values are defined in `docker-compose.yml` and `backend/src/main/resources/application-dev.yml`.

```text
Host: localhost
Port: 3306
Database: customer_management
Username: cms_user
Password: cms_password
```

Flyway owns schema changes. Hibernate is configured with `ddl-auto: validate` so the application fails fast if entity mappings drift from the migration scripts.

## Demo Data

Use the dev customer seed script when you want data for list/detail endpoint testing.

```bash
backend/scripts/seed-dev-customers.sh
```

What it does:

- deletes only customers whose NIC starts with `DEV-NIC-`
- inserts 8 deterministic dev customers
- adds mobile numbers and addresses
- adds a few customer family links
- prints the inserted customers

This script assumes the MariaDB container is running. It is safe to rerun.

## API Endpoints

Base URL:

```text
http://localhost:8080
```

Customer endpoints:

```text
GET    /api/v1/customers?page=0&size=10&sortBy=name&sortDir=asc
GET    /api/v1/customers/{id}
POST   /api/v1/customers
PUT    /api/v1/customers/{id}
DELETE /api/v1/customers/{id}
```

Master data endpoint:

```text
GET /api/v1/cities
```

Create customer request example:

```json
{
  "name": "Nimal Perera",
  "dateOfBirth": "1990-01-01",
  "nicNumber": "NIC-POSTMAN-001",
  "mobileNumbers": ["0771234567"],
  "addresses": [
    {
      "addressLine1": "Line 1",
      "addressLine2": "Line 2",
      "cityId": 1
    }
  ],
  "familyMemberIds": []
}
```

All API responses use the same envelope shape:

```json
{
  "success": true,
  "data": {},
  "message": "Operation completed successfully.",
  "timestamp": "2026-04-25T10:00:00"
}
```

Each completed HTTP request writes a body-free log line similar to:

```text
INFO [request-id] [http-nio-8080-exec-1] c.e.c.filter.RequestLoggingFilter - HTTP request completed method=GET path=/api/v1/customers status=200 durationMs=34
```

Request bodies and query strings are intentionally not logged because they can contain personal data.

## Postman Testing Flow

Postman artifacts are available under `docs/postman/`:

```text
customer-management-api.postman_collection.json
customer-management-local.postman_environment.json
```

Import both files into Postman and select the `Customer Management Local` environment. The collection parameterizes:

- `baseUrl`
- `customerId`
- `missingCustomerId`
- pagination and sorting query values
- create/update scalar fields
- `customerMobileNumbers` and `updatedCustomerMobileNumbers` as raw JSON arrays
- `customerAddresses` and `updatedCustomerAddresses` as raw JSON arrays of address objects
- `customerFamilyMemberIds` and `updatedCustomerFamilyMemberIds` as raw JSON arrays

The create and update requests intentionally inject the array fields as raw JSON, not quoted strings. That lets you test one or many mobile numbers, addresses, and family member IDs by changing only the environment values.

1. Start MariaDB.
2. Run the dev seed script.
3. Start the backend.
4. Import the Postman collection and environment.
5. Call `GET /api/v1/customers?page=0&size=20&sortBy=name&sortDir=asc`.
6. Copy a customer `id` from the list response into the `customerId` environment variable.
7. Call `GET /api/v1/customers/{id}` to verify detail loading.
8. Create, update, and delete a customer with a non-`DEV-NIC-*` NIC so seeded data stays easy to reset.

## Backend Tests

Run backend tests from `backend/`.

```bash
mvn test
```

The current backend test suite covers:

- application context startup
- request ID filter behavior
- global exception handling
- repository queries and JPA mappings
- customer service business rules
- customer and master data controller response shapes

## Commands

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

## Documentation Map

- `README.md` is the main monorepo setup and reviewer guide.
- `backend/README.md` contains backend-specific operational and implementation notes.
- `docs/postman/` contains the Postman collection and local environment for manual API testing.

## Troubleshooting

- If MariaDB does not start, make sure Docker Desktop is running and retry `docker compose up -d mariadb`.
- If the backend cannot connect to MariaDB, confirm `docker compose ps` shows the `mariadb` service as healthy.
- If port `8080` is already in use, stop the existing process or change `server.port` temporarily.
- If Swagger does not load, confirm the backend started successfully and open `http://localhost:8080/swagger-ui.html`.
- If the seed script fails, confirm the MariaDB container is running and the database credentials match `docker-compose.yml`.
