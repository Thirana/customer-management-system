# Customer Management Frontend

This frontend is a Vite + React application inside the monorepo.

Phase 1 provides:

- Vite React scaffold
- React Router route structure
- Axios API client
- shared API response helpers
- base application layout and navigation
- screen placeholders for customer list, create, detail, edit, and import flows

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

## Available Routes

- `/customers`
- `/customers/new`
- `/customers/:id`
- `/customers/:id/edit`
- `/customers/import`

The root route redirects to `/customers`.

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

The client matches the backend `ApiResponse<T>` envelope and surfaces backend validation or failure messages in a frontend-friendly format.
