# Customer Management Frontend

This frontend is a Vite + React application inside the monorepo.

The frontend currently provides:

- Vite React scaffold
- Tailwind-based warm-light UI system
- React Router route structure
- Axios API client
- shared API response helpers
- shared application shell and UI primitives
- backend-driven customer list view with pagination, sorting, and delete refresh
- reusable customer create/edit form with dynamic mobile, address, city, and searchable family-member inputs
- customer detail view with back, edit, delete, and linked family-member navigation
- import screen placeholders for the later bulk-import phase

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
- create/edit uses backend city lookup, debounced customer search for family-member selection, and backend validation messages
- `/customers/:id` loads the full customer profile with mobile numbers, addresses, family-member links, and delete behavior
- the shared shell, buttons, badges, sections, and list presentation use a warm-light product UI
- `/customers/import` remains the placeholder route for the later import UI phase

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
