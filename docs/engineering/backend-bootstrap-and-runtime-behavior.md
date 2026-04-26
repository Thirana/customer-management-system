# Backend Bootstrap and Runtime Behavior

## Scope

This note summarizes the runtime bootstrap behavior of the Spring Boot backend.

The focus is:

- startup sequence
- configuration loading
- database initialization
- shared request pipeline
- Swagger exposure
- async import runtime support

The goal is to explain what has to succeed before the backend starts accepting traffic, and what guarantees once startup completes.

## Startup Sequence

The backend starts in `Application.main(...)`, which delegates directly to `SpringApplication.run(...)`.

In practical terms, startup proceeds in this order:

- select the active Spring profile
- load application configuration
- create the Spring application context
- initialize infrastructure beans such as CORS, Jackson, filters, Swagger config, and async executor configuration
- initialize the datasource
- run Flyway migrations when enabled
- validate JPA mappings against the schema
- start the embedded web server on port `8080`

Operational effect:

- the backend does not begin serving HTTP requests until configuration, database access, and core runtime wiring are already in place

## Configuration Loading

The backend uses one shared base config plus profile specific overrides.

Current configuration files are:

- `application.yml`
- `application-dev.yml`
- `application-test.yml`
- `application-prod.yml`

`application.yml` defines the shared runtime defaults, including:

- default active profile
- multipart upload limits
- JPA validation mode
- Swagger UI path
- import settings
- async executor settings

The profile files then supply environment specific database and logging values.

The application also binds custom properties under the `app.*` prefix through `AppProperties`.

Those settings are validated during startup. That includes:

- frontend allowed origin
- import batch size
- import file size limit
- stored error limit for import jobs
- async executor pool sizing

Operational effect:

- invalid custom configuration fails startup early instead of creating a partially usable application

## Database Initialization

The `dev` profile uses MariaDB on `localhost:3306`. The `test` profile uses in-memory H2. The `prod` profile expects externally provided MariaDB connection values.

Database startup behavior follows a strict ownership model:

- Flyway owns schema creation and change history
- Hibernate validates mappings against the existing schema
- Hibernate does not create or update tables automatically

In the `dev` and `prod` profiles, the backend depends on a working MariaDB connection before startup can complete.

Operational effect:

- if the database is unavailable, the app does not start
- if the entity mappings drift from the migration defined schema, the app does not start
- once the app is listening, the database connection and schema validation steps have already succeeded

## Shared Runtime Pipeline

Once the application context is initialized, the backend exposes a shared request pipeline for every API request.

That pipeline includes:

- CORS rules for `/api/**`
- request ID generation or propagation through `X-Request-ID`
- one line request completion logging
- Jackson configuration for readable JSON date handling
- global exception handling with consistent API response envelopes

### Request ID behavior

The request ID filter does two things:

- preserves an incoming `X-Request-ID` when the client already sent one
- generates a new UUID when the client did not

The same value is returned in the response header and stored in the logging context for the lifetime of the request.

Operational effect:

- individual API requests can be traced reliably across logs and client side debugging

### Request completion logging

The request logging filter emits one log line per completed request with:

- method
- request path
- response status
- request duration

It intentionally does not log request bodies or query strings.

Operational effect:

- request activity is observable without printing personal customer data into logs

### JSON and error handling behavior

Jackson is configured to keep dates as readable ISO style strings instead of numeric timestamps.

The global exception handler normalizes:

- expected application errors
- request validation failures
- malformed request bodies
- upload size violations
- unexpected server errors

Operational effect:

- clients receive a consistent response shape
- internal stack traces stay server-side

## API Documentation Exposure

OpenAPI metadata is registered through the backend Swagger configuration.

The Swagger UI is exposed at:

```text
/docs
```

The OpenAPI document describes the same running application that serves the API. If the backend fails during startup, the documentation UI is not available either.

Operational effect:

- the documentation endpoint is tied to real application startup success rather than being a separate static artifact

## Async Import Runtime Support

Bulk import processing does not run on the HTTP request thread.

Instead, the backend uses a dedicated bounded async executor for import jobs. Its purpose is to:

- return the upload request quickly
- move file processing into background worker threads
- prevent long-running imports from consuming normal request-handling capacity

The executor also copies request-scoped logging context into the worker thread.

Operational effect:

- import jobs can continue after the upload response is returned
- logs written during import processing still carry the originating request ID
- import throughput is controlled by configured thread-pool limits rather than unbounded concurrency

## Runtime Guarantees

Once the backend has completed startup and is accepting traffic, the following assumptions hold:

- application configuration loaded successfully
- custom `app.*` settings passed validation
- datasource initialization succeeded
- Flyway ran when enabled for the active profile
- JPA mappings matched the schema
- request tracing is active through `X-Request-ID`
- request completion logging is active
- JSON date serialization is consistent
- exception responses are normalized
- Swagger UI is exposed at `/docs`
- async import is ready

This creates a strong startup contract.

The backend accepts traffic only after the platform layer, database layer, and request pipeline are fully initialized.
