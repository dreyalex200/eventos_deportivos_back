# Business Context

The Geo Service provides standardized geographic metadata required by various platform services to ensure consistency across the ecosystem.

# Service Purpose

To manage, distribute, and enforce data integrity for geographic entities such as countries, subdivisions, timezones, languages, postal codes, calling codes, currencies, and country applications. Boundaries include all geographic metadata excluding user-specific location tracking.

# Bounded Context

Domain ownership: Geographic Metadata (Countries, Subdivisions, Timezones, Languages, Postal Codes, Calling Codes, Currencies, Country Applications).

# Database Model

Entities:

* Countries
* Subdivisions
* Timezones
* Languages
* Postal Codes
* Calling Codes
* Currencies
* Country Applications

For each entity document:

* Purpose: Provides standardized data for platform consumption.
* Fields: Standard `id`, domain specific fields (e.g. `iso2`, `name`, `code`), audit fields (`created_at`, `updated_at`, `deleted_at`).
* Relationships: Hierarchical relationships where applicable.
* Constraints: Unique ISO and standardized codes.
* Validation Rules: Strict format checking and referential integrity.

# Relationships

* Subdivisions belong to Countries
* Postal Codes belong to Subdivisions/Countries
* Country Applications map Applications to Countries
* Currencies, Languages, Timezones, Calling Codes can be associated with Countries.

# Business Rules

* Soft deleted records are excluded from public client APIs.
* Unique constraints on standardized codes (e.g., ISO2, ISO3).
* Status management governs visibility across the platform.

# Security Rules

* Authentication: Required for Admin API (JWT). Client API may be public or require a minimal token.
* Authorization: Admin requires specific Write permissions.
* JWT requirements: Standard claims verification (`sub`, `exp`, `roles`, `permissions`).
* Permission model: E.g., `API_GEO_COUNTRIES_READ`, `API_GEO_COUNTRIES_WRITE`.

# Audit Model

Fields: `created_at`, `updated_at`, `deleted_at`, `created_by`, `updated_by`, `deleted_by`.

# Soft Delete Workflow

When soft deleted, `deleted_at` is populated. The entity is hidden from non-protected endpoints but remains for historical integrity.

# Status Change Workflow

Admins can toggle `is_active`. Inactive items are hidden from public queries.

# Endpoint Catalog

### Health Check
```http
GET /health
```
Mandatory endpoint to verify the operational status of the service.

Base pattern: `/api/v1/geo/{role}/{resource}`

Resources include:
* `countries`
* `subdivisions`
* `timezones`
* `languages`
* `postal-codes`
* `calling-codes`
* `currencies`
* `country-applications`

Include operations per resource:

### List
```http
GET /api/v1/geo/{role}/{resource}?limit=20&offset=0&trash=false&q=
```

### Get By Id
```http
GET /api/v1/geo/{role}/{resource}/{id}
```

### Create
```http
POST /api/v1/geo/{role}/{resource}
```

### Update
```http
PUT /api/v1/geo/{role}/{resource}/{id}
```

### Change Status
```http
POST /api/v1/geo/{role}/{resource}/{id}/change-status
```

### Soft Delete
```http
DELETE /api/v1/geo/{role}/{resource}/{id}/delete-soft
```

### Hard Delete
```http
DELETE /api/v1/geo/{role}/{resource}/{id}/delete-hard
```

### Restore
```http
POST /api/v1/geo/{role}/{resource}/{id}/restore
```

# Dependency Management

* Dependency registration: No external service dependencies beyond the database.
* Dependency validation: Internal module validation only.
* Dependency removal: Cascading deletes governed by business rules.
* Dependency version compatibility: Standard semantic versioning for the API.

# API Consumption Management

* API consumption tracking: Request logging and metrics.
* Version compatibility: Major version reflected in URL (`v1`).
* Authorization requirements: Documented per endpoint context.
