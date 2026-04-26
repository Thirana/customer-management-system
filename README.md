# Customer Management System

Customer Management System is a monorepo for a software engineer interview assignment. The repository currently contains a Spring Boot backend, Docker-managed MariaDB for local development, Postman artifacts for API testing, and import tooling for Excel-based bulk customer onboarding. The frontend is still pending.

## Current Status

- Backend is implemented through Phase 7
- Spring Boot 2.7.18 API with Java 8 target compatibility
- MariaDB + Flyway schema management
- Customer CRUD, city lookup, and async Excel import APIs
- Swagger UI at `http://localhost:8080/docs`
- Postman collection and local environment under `docs/postman/`
- Backend automated tests, sample import workbook, and smoke-test guidance

## Repository Structure

```text
customer-management-system/
├── backend/                 Spring Boot API project
│   ├── examples/
│   ├── pom.xml
│   ├── README.md
│   ├── scripts/
│   └── src/
├── docs/postman/            Importable Postman collection and environment
├── docs/testing/            Backend automated and smoke-test guides
├── docker-compose.yml       Local MariaDB service
├── README.md                Monorepo setup and reviewer entrypoint
└── doc/                     Local planning docs, ignored by Git
```

## Prerequisites

- Java
- Maven
- Docker Desktop or another running Docker daemon
- Postman or another API client for manual testing

The backend compiles with Java 8 target compatibility and runs locally on modern JDKs such as Java 21.

## Local Setup

1. Start MariaDB from the repository root.

```bash
docker compose up -d mariadb
```

2. Confirm the container is running.

```bash
docker compose ps
```

3. Start the backend.

```bash
cd backend
mvn spring-boot:run
```

4. Open Swagger UI.

```text
http://localhost:8080/docs
```

The default active profile is `dev`. Flyway runs automatically on startup and applies the schema plus master data.

## Database

Local MariaDB values are defined in `docker-compose.yml` and `backend/src/main/resources/application-dev.yml`.

```text
Host: localhost
Port: 3306
Database: customer_management
Username: cms_user
Password: cms_password
```

Flyway owns schema changes. Hibernate runs with `ddl-auto: validate` for real database profiles.

## API Overview

Base URL:

```text
http://localhost:8080
```

Available backend endpoints:

```text
GET    /api/v1/cities
GET    /api/v1/customers
GET    /api/v1/customers/{id}
POST   /api/v1/customers
PUT    /api/v1/customers/{id}
DELETE /api/v1/customers/{id}
POST   /api/v1/customers/import
GET    /api/v1/customers/import/{jobId}/status
```

For backend-specific API behavior, request/response details, logging notes, and import rules, see [backend/README.md](/Users/thiranaembuldeniya/Documents/SE/Projects/customer-management-system/backend/README.md:1).

## Dev Scripts and Import Files

Seed dev customers:

```bash
backend/scripts/seed-dev-customers.sh
```

Generate a large import workbook:

```bash
backend/scripts/generate-import-workbook.sh --output=backend/examples/generated/customers-import-10000.xlsx --rows=10000 --mode=mixed
```

Committed sample workbook:

```text
backend/examples/customers-import-sample.xlsx
```

### Script Compatibility

The shell script entrypoints work on:

- macOS
- Linux
- Windows with Git Bash or WSL

They do not run cleanly from plain Command Prompt or PowerShell.

The Excel generator core is [generate_import_workbook.py](/Users/thiranaembuldeniya/Documents/SE/Projects/customer-management-system/backend/scripts/generate_import_workbook.py:1), which uses only Python standard library modules. That script is cross-platform if Python 3 is installed.

Windows example:

```powershell
python backend/scripts/generate_import_workbook.py --output=backend/examples/generated/test.xlsx --rows=1000 --mode=mixed
```

## Postman

Postman artifacts are available under:

```text
docs/postman/customer-management-api.postman_collection.json
docs/postman/customer-management-local.postman_environment.json
```

Import both files and select the `Customer Management Local` environment before sending requests.

## Testing

Detailed backend testing guides:

- [Backend automated testing](/Users/thiranaembuldeniya/Documents/SE/Projects/customer-management-system/docs/testing/backend-automated-testing.md:1)
- [Backend smoke testing](/Users/thiranaembuldeniya/Documents/SE/Projects/customer-management-system/docs/testing/backend-smoke-testing.md:1)

## Common Commands

From the repository root:

```bash
docker compose up -d mariadb
docker compose ps
docker compose down
backend/scripts/seed-dev-customers.sh
backend/scripts/generate-import-workbook.sh --output=backend/examples/generated/customers-import-10000.xlsx --rows=10000 --mode=mixed
```

From `backend/`:

```bash
mvn test
mvn spring-boot:run
```

## Documentation Map

- `README.md` is the main monorepo setup and reviewer guide
- `backend/README.md` contains backend-specific implementation and operational notes
- `docs/postman/` contains Postman artifacts for manual API testing
- `docs/testing/` contains backend automated and smoke-test guides
