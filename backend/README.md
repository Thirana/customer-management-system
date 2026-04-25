# Customer Management Backend

Spring Boot REST API for the Customer Management System.

## Requirements

- Java 8 compatible runtime for assignment verification
- Maven
- MariaDB

## Run Locally

Create a local database named `customer_management`, then update `src/main/resources/application-dev.yml` if your MariaDB credentials differ from the defaults.

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## API Docs

Swagger UI will be available at:

```text
http://localhost:8080/swagger-ui.html
```

