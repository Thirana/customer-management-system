# Customer Management Backend

Spring Boot REST API for the Customer Management System.

## Requirements

- Java 8 compatible runtime for assignment verification
- Maven
- MariaDB

## Run Locally

Start MariaDB from the repository root:

```bash
docker compose up -d mariadb
```

The dev profile expects:

```text
Database: customer_management
Username: cms_user
Password: cms_password
Port: 3306
```

Then run the backend from this folder:

```bash
mvn spring-boot:run
```

Flyway applies database migrations automatically on startup.

## Test

```bash
mvn test
```

## API Docs

Swagger UI will be available at:

```text
http://localhost:8080/swagger-ui.html
```
