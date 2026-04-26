# Backend Automated Testing

This document covers the automated backend test workflow for the Customer Management System.

## Run the Test Suite

From `backend/`:

```bash
mvn test
```

Expected result:

- all backend tests pass
- Flyway backed schema assumptions remain valid
- request/response behavior, business rules, and import flow stay covered

## Current Coverage

The current backend test covers:

- application context startup
- request ID filter behavior
- request completion logging filter behavior
- global exception handling
- repository queries and JPA mappings
- customer service business rules
- city lookup and customer controller responses
- bulk import service, processor, and controller flows
- end to end CRUD integration flow on the test profile

## Test Stack Notes

- JUnit 5 is used for automated backend testing
- repository and integration coverage use the existing H2-based test profile
- production-like schema structure is still defined through Flyway migrations

## When to Run

Run `mvn test` at minimum:

- before committing backend changes
- after changing entities, repositories, DTOs, or service rules
- after changing import behavior
- before handing the assignment to a reviewer
