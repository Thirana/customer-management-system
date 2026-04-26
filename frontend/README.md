# Customer Management Frontend

This frontend is a Vite + React application inside the monorepo.

The frontend currently provides:

- Vite React scaffold
- Tailwind based UI system
- React Router route structure
- Axios API client
- shared API response helpers
- shared application shell and UI primitives
- backend driven customer list view with pagination, sorting,search, and delete refresh
- reusable customer create/edit form with dynamic mobile, address, city, and searchable family member inputs
- customer detail view with back, edit, delete, and linked family member navigation
- bulk import screen with `.xlsx` upload, async status polling, progress summary, and row level errors

## Environment

Create a local `.env` file from `.env.example` when needed.

```text
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Run Locally

From `frontend/`:

```bash
npm install
npm run dev
```

Default local URL:

```text
http://localhost:5173
```

Keep the frontend on `http://localhost:5173` unless you also update the backend CORS origin. The backend default local CORS setting expects this exact origin.

## Available Routes

- `/customers`
- `/customers/new`
- `/customers/:id`
- `/customers/:id/edit`
- `/customers/import`

The root route redirects to `/customers`.

## Current UI Coverage

- `/customers` loads paginated customer summaries from the backend
- list controls keep `page`, `size`, `sortBy`, and `sortDir` synchronized with the URL
- `/customers/new` and `/customers/:id/edit` support shared create/edit submission flow
- create/edit uses backend city lookup, debounced customer search for family member selection, and backend validation messages
- `/customers/:id` loads the full customer profile with mobile numbers, addresses, family member links, and delete behavior
- `/customers/import` uploads `.xlsx` workbooks, polls import status every 2 seconds, and shows completion counts plus row-level errors

## API Client

The shared API client is prepared for these backend operations:

- get customers
- get customer detail
- create customer
- update customer
- delete customer
- get cities
- upload import workbook
- poll import status

The client matches the backend `ApiResponse<T>` envelope and surfaces backend validation or failure messages in a frontend friendly format.

## Import Notes

- The import page accepts `.xlsx` files only
- The expected sheet columns are `Name`, `Date of Birth`, `NIC Number`, and optional `Operation`
- `Operation` supports `CREATE` and `UPDATE`
- The page polls the backend import job every 2 seconds until it reaches `COMPLETED` or `FAILED`
- A committed sample workbook is available at `backend/examples/customers-import-sample.xlsx`
- Reusing the same sample workbook on the same local database can show a duplicate NIC error for the create row. Run `docker compose down -v` from the repository root when you want a clean local import reset.

## Manual Smoke Checklist

1. Start MariaDB, backend, and frontend.
2. Open `/customers` and confirm the list loads.
3. Create a customer from `/customers/new`.
4. Open a customer detail page and confirm edit and delete actions work.
5. Open `/customers/import`.
6. Upload `backend/examples/customers-import-sample.xlsx`.
7. Confirm progress, counts, and row-level errors update.
8. Return to `/customers` and confirm imported customer changes appear in the list.
