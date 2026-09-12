# Feature: Countries Management

## Metadata

| Field      | Value           |
| ---------- | --------------- |
| Feature ID | GEO-COUNTRY-001 |
| Domain     | Geo             |
| Module     | Countries       |
| Version    | 1.0.0           |
| Status     | Active          |
| Priority   | High            |

## Related Rules

* .ai/rules/architecture/hexagonal.md
* .ai/rules/architecture/clean-architecture.md
* .ai/rules/architecture/dependency-rules.md
* .ai/rules/architecture/folder-structure.md
* .ai/rules/standards/code-style.md
* .ai/rules/standards/security-rules.md
* .ai/rules/standards/jwt-structure.md
* .ai/rules/standards/http-responses/*

---

# Business Objective

Provide a centralized and standardized catalog of countries that can be consumed by all platform modules.

Examples:

* Users
* Companies
* Addresses
* Billing
* Logistics
* Localization

---

# Domain Entity

## Country

| Field        | Type      |
| ------------ | --------- |
| id           | UUID      |
| iso2         | String    |
| iso3         | String    |
| numeric_code | String    |
| name         | String    |
| flag_url     | String    |
| flag_emoji   | String    |
| is_active    | Boolean   |
| created_at   | Timestamp |
| updated_at   | Timestamp |
| deleted_at   | Timestamp |

---

# Permissions

Read:

```text
API_GEO_COUNTRIES_READ
```

Write:

```text
API_GEO_COUNTRIES_WRITE
```

---

# Business Rules

## BR-001

ISO2 must be unique.

## BR-002

ISO3 must be unique.

## BR-003

Client APIs only expose active countries.

```text
is_active = true
deleted_at IS NULL
```

## BR-004

Deleted countries cannot be returned by public endpoints.

## BR-005

A country can only be restored when deleted_at IS NOT NULL.

## BR-006

Country flags must be stored under:

```text
arcadion-realm/assets/images/countries/
```

## BR-007

Changing a flag replaces the previous file.

---

# Supported Operations

## Client

* List Countries
* Get Country By ID
* Get Country By ISO2
* Search Countries
* Autocomplete Countries

## Administration

* List Countries
* Get Country
* Create Country
* Update Country
* Change Status
* Soft Delete
* Hard Delete
* Restore

---

# BDD Scenarios

## Scenario: Create Country Successfully

Given an administrator with permission API_GEO_COUNTRIES_WRITE

And a valid country payload

When the administrator creates a country

Then the country is persisted

And HTTP 201 is returned

---

## Scenario: Reject Duplicate ISO2

Given an existing country with ISO2 CO

When a new country is created using ISO2 CO

Then the operation is rejected

And error country_already_registered is returned

---

## Scenario: List Public Countries

Given active countries exist

And deleted countries exist

When a client requests countries

Then only active countries are returned

And deleted countries are excluded

---

## Scenario: Restore Country

Given a country is soft deleted

When an administrator restores the country

Then deleted_at becomes null

And HTTP 200 is returned

---

# API Contracts

See:

* [.ai/specs/services/api-geo.md](../services/api-geo.md)
* [.ai/specs/services/service-conventions.md](../services/service-conventions.md)

---

# Acceptance Criteria

## AC-001

Country creation succeeds with valid data.

## AC-002

ISO2 duplication is rejected.

## AC-003

ISO3 duplication is rejected.

## AC-004

Soft deleted countries are hidden from client APIs.

## AC-005

Countries can be restored.

## AC-006

Flag replacement removes the previous file.

---

# Related Tasks

* TASK-GEO-COUNTRY-001 -> . ai/specs/tasks/GEO-COUNTRY-001/TASK-001-domain.md
* TASK-GEO-COUNTRY-002 -> .ai/specs/tasks/GEO-COUNTRY-001/TASK-002-repository.md
* TASK-GEO-COUNTRY-003 -> .ai/specs/tasks/GEO-COUNTRY-001/TASK-003-use-cases.md
* TASK-GEO-COUNTRY-004 -> .ai/specs/tasks/GEO-COUNTRY-001/TASK-004-protected-api.md
* TASK-GEO-COUNTRY-005 -> .ai/specs/tasks/GEO-COUNTRY-001/TASK-005-client-api.md
* TASK-GEO-COUNTRY-006 -> .ai/specs/tasks/GEO-COUNTRY-001/TASK-006-storage.md
* TASK-GEO-COUNTRY-007 -> .ai/specs/tasks/GEO-COUNTRY-001/TASK-007-tests.md
* TASK-GEO-COUNTRY-008 -> .ai/specs/tasks/GEO-COUNTRY-001/TASK-008-documentation.md


## Related Documentation

### Functional Documentation

- docs/01_functionality_docs/countries-docs.md

### API Documentation

- docs/02_api/endpoints.md
- docs/02_api/drey-Geo.postman_collection.json

## Traceability

This feature is functionally documented in the corresponding Functional Documentation and its API behavior is defined in the API documentation. Any implementation changes affecting this feature must keep these documents synchronized.
