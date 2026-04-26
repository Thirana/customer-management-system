# Backend Database Schema Overview

## Scope

This note summarizes the MariaDB schema used by the Customer Management System backend.

The focus is:

- the main tables
- how they relate to each other
- the most important constraints and indexes
- why the schema is structured this way

The schema is created and evolved through Flyway migrations rather than automatic ORM schema generation.

## Schema Inventory

The backend currently uses these main business tables:

- `countries`
- `cities`
- `customers`
- `mobile_numbers`
- `addresses`
- `customer_family`

Flyway also maintains its own schema history table for migration tracking.

## Relationship Model

The current relationship model is:

- one country to many cities
- one customer to many mobile numbers
- one customer to many addresses
- one address to one city
- one customer to many family links
- one customer can also appear as the family member in another customer’s link set

Operationally, `customers` is the root record. Mobile numbers, addresses, and family links are dependent data around that root.

## Master Data Tables

### Countries

The `countries` table is the top level location reference table.

It stores:

- a generated primary key
- a unique country name

Its role is simple but important:

- it keeps location reference data consistent
- it prevents country values from being duplicated as free text strings in customer records

### Cities

The `cities` table stores individual cities and links each city to one and only one country.

Important design choices:

- the same city name can exist in different countries
- the same city name cannot be duplicated within the same country
- every city must belong to a valid country

The project also seeds a useful set of countries and cities through Flyway so the application can support dropdown based address entry immediately.

Operational effect:

- address data stays consistent
- frontend forms can rely on stable master data
- customer addresses do not need to duplicate country information when city already determines it

## Customer Aggregate Tables

### Customers

The `customers` table stores the root customer record.

Main business columns are:

- `name`
- `date_of_birth`
- `nic_number`

It also stores audit timestamps:

- `created_at`
- `updated_at`

This table represents the customer’s core identity.

### Mobile Numbers

The `mobile_numbers` table stores repeatable mobile number entries for a customer.

This is kept in a child table because a customer can have more than one mobile number.

Operational effect:

- the data model supports one or many phone numbers without forcing fixed numbered columns into the customer root table

### Addresses

The `addresses` table stores repeatable address records for a customer.

Each address links to one city through `city_id`.

Country is not stored directly on the address because it is already derived through the city relationship.

Operational effect:

- the schema avoids duplicating country values
- the same customer can store multiple addresses cleanly

### Customer Family

The `customer_family` table models family links between customers.

It is a join table with:

- `customer_id`
- `family_member_id`

This design allows one customer to reference several other customers as family members without embedding family data into the root customer row.

Operational effect:

- family relationships stay flexible
- the backend can represent real customer-to-customer links rather than storing family names as plain text

## Important Constraints

The schema uses a small set of important constraints to protect core business rules.

### NIC uniqueness

`customers.nic_number` is unique.

Operational effect:

- the schema enforces one customer identity per NIC
- duplicate customer creation cannot succeed even if application layer validation is bypassed

### Required foreign keys

The schema requires valid parent references for:

- `cities.country_id`
- `mobile_numbers.customer_id`
- `addresses.customer_id`
- `addresses.city_id`
- both sides of `customer_family`

Operational effect:

- child rows cannot point to missing customers, missing cities, or missing countries

### Self reference protection in family links

The `customer_family` table includes a check to prevent a customer from being linked to itself as a family member.

Operational effect:

- the database protects an important relationship invariant even if the application layer misses it

### Cascading child cleanup

Customer owned child records are configured to delete with the parent customer where appropriate.

Operational effect:

- deleting a customer does not leave orphaned mobile number or address records behind

## Important Indexes

The schema adds targeted indexes for the main read and write paths.

### NIC lookup

The unique NIC index supports:

- create validation
- update validation
- import resolution by NIC

This is one of the most important indexes in the system because normal writes and bulk imports both depend on fast NIC-based lookups.

### Customer list support

Indexes on customer name and creation time support:

- predictable list sorting
- stable pagination behavior

These indexes align with how customers are typically browsed in list views.

### Join support

Indexes on child foreign keys support:

- loading mobile numbers by customer
- loading addresses by customer
- resolving city usage from addresses
- loading cities by country

This matters because detail views reconstruct a customer profile from several normalized tables.

### Family relationship support

The reverse lookup index on `customer_family.family_member_id` supports queries where the backend needs to find customers that reference a given family member.

Operational effect:

- self-referencing relationship lookups remain practical as the dataset grows

## Operational Guarantees

The current schema creates these practical guarantees:

- no two customers can share the same NIC
- customer addresses can only reference valid cities
- valid cities can only reference valid countries
- customer detail views can rebuild a full profile from normalized tables
- one customer can store multiple phone numbers and multiple addresses cleanly
- family relationships are modeled as real links between customer records
- the schema supports both CRUD operations and bulk import lookups efficiently
