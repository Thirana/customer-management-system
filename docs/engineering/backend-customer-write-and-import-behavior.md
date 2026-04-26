# Backend Customer Write and Import Behavior

## Scope

This note summarizes the main backend write behavior for customer data.

The focus is:

- customer create behavior
- customer update behavior
- customer delete behavior
- validation rules around related data
- async Excel import behavior

The goal is to explain how the backend handles normal customer writes and how that behavior changes for bulk import jobs.

## Service Responsibility

The backend keeps controllers thin.

In practical terms, the service layer is responsible for:

- validating business rules after request-shape validation
- checking related entities such as cities and family members
- enforcing NIC uniqueness rules
- coordinating persistence of the customer root and child collections
- shaping the response through backend mapping rules

Operational effect:

- business behavior is centralized in one layer rather than scattered across controllers and repositories

## Customer Create Behavior

Customer creation starts with a validated request body and then applies backend-side business checks before persistence.

The create path performs these checks and actions:

- reject the request if another customer already uses the same NIC
- copy the core customer fields into a new customer record
- normalize the submitted mobile number list
- validate each submitted address city ID
- validate each submitted family member ID
- persist the customer root record together with its child collections
- flush the persistence context before building the response

Addresses, mobile numbers, and family members are treated as part of the same write operation.

Operational effect:

- the client receives one consistent customer profile response
- invalid related IDs are rejected before any partial customer data is accepted
- generated child row IDs are available in the final response

## Customer Update Behavior

Customer updates start by loading the existing customer record.

The update path then:

- rejects the request if the target customer does not exist
- re-checks NIC uniqueness while excluding the current customer from the conflict check
- replaces the current mobile number collection with the submitted list
- replaces the current address collection with the submitted list
- replaces the current family member collection with the submitted list
- validates all submitted related IDs again
- flushes the updated state before mapping the response

This is intentionally a full-profile update model rather than a partial patch model.

Operational effect:

- the backend always returns the customer’s current stored profile after the update
- old child rows do not linger when the client submits a replacement set of mobile numbers or addresses

## Validation Rules

The customer write path enforces a small set of important business rules.

### Core request rules

The incoming request must include the required customer fields expected by the DTO layer.

Operational effect:

- obviously incomplete customer payloads are rejected before persistence logic runs

### NIC uniqueness

The backend rejects:

- create requests for an already used NIC
- update requests that would move a customer onto another customer’s NIC

Operational effect:

- the backend preserves one customer identity per NIC at both the application and database layers

### Valid city references

Every submitted address must point to a real city.

Operational effect:

- addresses cannot be saved against missing or invented location IDs

### Valid family member references

Every submitted family member ID must point to a real customer.

Operational effect:

- family links remain real customer-to-customer relationships rather than loose identifiers

### No self-reference

A customer cannot be added as their own family member during update.

Operational effect:

- the application rejects an invalid relationship before the database check would need to catch it

### Supported list sorting

The list service also rejects unsupported sort fields.

This is not part of the write path itself, but it follows the same service-level rule enforcement pattern.

## Customer Delete Behavior

Customer delete behavior is intentionally simple.

The backend:

- loads the target customer
- clears direct family member links from the loaded entity
- deletes the customer record

The schema then handles child cleanup for dependent data through the mapped relationships and foreign key behavior.

Operational effect:

- the backend removes the customer without leaving related contact rows behind

## Bulk Import Flow

The bulk import feature is the backend’s most distinctive write path.

The upload endpoint accepts `.xlsx` files and performs only the fast, request-time work needed to accept the job safely.

That request-time flow does this:

- reject empty uploads
- reject missing filenames
- reject non-`.xlsx` files
- reject files that exceed the configured size limit
- stage the uploaded file to temporary disk storage
- create an in-memory import job record
- return `202 Accepted` with a job ID

The actual file processing happens after the upload response is returned.

Operational effect:

- the client gets a fast acknowledgment
- long-running Excel processing does not block the request thread

## Import Parsing and Resolution

The import processor reads only the first sheet in the workbook.

Expected columns are:

- `Name`
- `Date of Birth`
- `NIC Number`
- `Operation`

The first three columns are required. The fourth column is optional, but if present it must be named `Operation`.

Supported row behavior is:

- `CREATE` means the row must create a new customer
- `UPDATE` means the row must update an existing customer
- blank `Operation` means the backend decides by NIC existence

When `Operation` is omitted:

- missing NIC becomes create
- existing NIC becomes update

The import processor supports Excel date cells and `yyyy-MM-dd` text values for date of birth.

It also ignores completely blank rows.

Operational effect:

- the import file stays simple for reviewers to understand
- the backend supports both explicit and convenience-style import behavior

## Import Write Scope

The Excel import contract is intentionally narrower than the normal customer API.

Import rows currently write only the core customer columns:

- name
- date of birth
- NIC number

The import flow does not manage:

- mobile numbers
- addresses
- family members

Operational effect:

- bulk import remains predictable and lightweight
- the backend avoids inventing a much more complex spreadsheet format for nested customer data

## Import Job Lifecycle

Every accepted import receives a job ID and immediately enters tracked processing.

The status tracker records:

- current status
- processed row count
- success count
- failure count
- total row count
- progress percentage
- stored row-level errors

The observed lifecycle is:

- `PROCESSING`
- `COMPLETED`
- `FAILED`

Operational effect:

- clients can poll for progress instead of waiting on one long HTTP request
- import behavior remains visible even though processing happens asynchronously

## Row-Level Error Handling

The import processor does not fail the whole file just because one row is bad.

Instead, it:

- validates rows one by one
- records row-specific failures
- skips invalid rows
- continues with the rest of the file

Examples of row-level failures include:

- missing name
- missing or invalid date of birth
- missing NIC
- duplicate NIC inside the same import batch
- `CREATE` row for an already existing NIC
- `UPDATE` row for a missing NIC
- invalid `Operation` value

Stored row errors are capped by configuration to keep memory usage bounded for large files.

Operational effect:

- large imports stay reviewable and resilient
- one bad row does not collapse the rest of the batch

## Batch Processing Behavior

The processor works in batches instead of saving each row one by one.

For each batch, it:

- collects NIC numbers
- loads existing customers for those NICs in one query
- resolves create versus update behavior
- saves the batch
- flushes changes
- clears the persistence context before the next batch

Operational effect:

- large files avoid an inefficient one-query-per-row pattern
- memory growth is controlled during long imports

## Operational Guarantees

The current customer write design creates these guarantees:

- create and update requests enforce NIC uniqueness
- addresses can only reference valid cities
- family links can only reference valid customers
- self-family links are rejected
- delete operations remove the customer cleanly with dependent contact data
- import uploads return quickly with a job ID
- import progress can be polled at any time
- existing customers are resolved by NIC during import
- invalid import rows are isolated instead of stopping the whole file
- bulk import remains simpler than full API writes by limiting itself to core customer fields

Together, these behaviors make the backend predictable for both normal API usage and bulk onboarding flows.
