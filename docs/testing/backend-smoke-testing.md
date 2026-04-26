# Backend Smoke Testing

This document covers the manual backend verification flow for the Customer Management System.

## Preparation

From the repository root:

```bash
docker compose up -d mariadb
backend/scripts/seed-dev-customers.sh
```

From `backend/`:

```bash
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/docs
```

## Manual Smoke Flow

1. Open Swagger UI or Postman.
2. Verify `GET /api/v1/cities` returns seeded city data.
3. Verify `GET /api/v1/customers` returns seeded `DEV-NIC-*` customers.
4. Create a new non-seeded customer.
5. Open `GET /api/v1/customers/{id}` for the created customer.
6. Update that customer and verify the changed values in detail and list views.
7. Delete that customer and confirm it no longer appears in the list.
8. Upload `backend/examples/customers-import-sample.xlsx`.
9. Poll `GET /api/v1/customers/import/{jobId}/status` until the job reaches `COMPLETED` or `FAILED`.
10. Confirm the sample workbook produces:
    - successful row processing
    - one expected invalid row error
11. Confirm responses include `X-Request-ID`.
12. Confirm logs show request IDs and do not print NIC, date of birth, mobile numbers, or addresses.

## Postman Artifacts

Use:

```text
docs/postman/customer-management-api.postman_collection.json
docs/postman/customer-management-local.postman_environment.json
```

Import both files and select the `Customer Management Local` environment.

## Large Import Check

Generate a larger workbook when needed:

```bash
backend/scripts/generate-import-workbook.sh --output=backend/examples/generated/customers-import-10000.xlsx --rows=10000 --mode=mixed
```

Use `create-only` or `auto` mode when you want import testing without depending on seeded `DEV-NIC-*` records.
