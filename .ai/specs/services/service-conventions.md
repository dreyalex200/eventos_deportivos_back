# Service Conventions

## API Structure

Standard Endpoint Pattern

```text
/api/{api_version}/{service_name}/{role}/{controller_name}
```

Example:

```text
/api/v1/geo/protected/countries
```

Parameter definitions:

| Parameter       | Description              |
| --------------- | ------------------------ |
| api_version     | API version              |
| service_name    | Service identifier       |
| role            | Authorized role          |
| controller_name | Resource/controller name |

---

## Controller Naming Convention

Controllers must be named using plural nouns in kebab-case (e.g., `countries`, `postal-codes`).

---

## Resource Naming Convention

Resources should reflect the domain entity and be written in snake_case in payloads, and kebab-case in URLs.

---

## Pagination

Standard query parameters:

```text
?limit=20&offset=0
```

Definitions:

* limit: The maximum number of records to return.
* offset: The number of records to skip before starting to collect the result set.

---

## Filtering

```text
?trash=false
```

Definitions and usage:
* trash: When true, includes soft-deleted records in the response. By default, it is false.

---

## Search

```text
?q=
```

Definitions and usage:
* q: A general search term to query against indexed fields of the resource.

---

## Sorting

Document sorting conventions.

Example:

```text
?sort=name&direction=asc
```

Definitions and usage:
* sort: The field name to sort by.
* direction: The order of sorting (`asc` or `desc`).

---

## JWT Requirements

* Authentication flow: Standard OAuth2/OIDC flow providing a Bearer token.
* JWT validation: Validate signature, issuer, and expiration.
* Required claims: `sub`, `exp`, `roles`, `permissions`.
* Token expiration handling: Reject expired tokens with 401 Unauthorized.

---

## Authorization

* Roles: User roles defined within the JWT.
* Permissions: Fine-grained permissions (e.g., `API_GEO_COUNTRIES_WRITE`).
* Access validation: Controllers must enforce required permissions prior to executing use cases.

---

## Audit Requirements

* created_at: Timestamp of record creation.
* updated_at: Timestamp of last update.
* deleted_at: Timestamp of soft deletion (null if active).
* created_by: User ID who created the record.
* updated_by: User ID who last updated the record.
* deleted_by: User ID who soft-deleted the record.

---

## Status Management

* Active: Normal operational state.
* Inactive: Disabled state, cannot be utilized.
* Pending: Awaiting activation or review.
* Archived: Kept for historical purposes but no longer actively used.

---

## Soft Delete

Endpoint:

```text
DELETE /{id}/delete-soft
```

Workflow and business rules: Sets `deleted_at` timestamp. Resource is hidden from public client endpoints.

---

## Hard Delete

Endpoint:

```text
DELETE /{id}/delete-hard
```

Workflow and business rules: Permanently removes the record from the database. Might require strict authorization.

---

## Restore

Endpoint:

```text
POST /{id}/restore
```

Workflow and business rules: Resets `deleted_at` to null. Re-activates the resource for client endpoints.

---

## Change Status

Endpoint:

```text
POST /{id}/change-status
```

Workflow and business rules: Modifies the operational status (e.g., from Active to Inactive).

---

## Standard Response Structure

Success response format:
```json
{
  "data": {},
  "meta": {
    "limit": 20,
    "offset": 0,
    "total": 100
  }
}
```

Error response format:
```json
{
  "error": {
    "code": "error_code",
    "message": "Human readable message"
  }
}
```

Validation response format:
```json
{
  "error": {
    "code": "validation_failed",
    "message": "Validation failed",
    "details": [
      {
        "field": "name",
        "issue": "is required"
      }
    ]
  }
}
```

---

## Error Handling

* Validation errors: Return 400 Bad Request with field-level details.
* Authorization errors: Return 403 Forbidden.
* Authentication errors: Return 401 Unauthorized.
* Business errors: Return 409 Conflict or 422 Unprocessable Entity.
* Internal server errors: Return 500 Internal Server Error.

---

## Standard Endpoint Catalog

### Health Check

Every microservice must expose a mandatory health check endpoint to verify its operational status. This is required for all current and future microservices developed under this standard.

```http
GET /health
```

### List

```http
GET /api/{api_version}/{service_name}/{role}/{controller_name}?limit=20&offset=0&trash=false&q=
```

### Get By Id

```http
GET /api/{api_version}/{service_name}/{role}/{controller_name}/{id}
```

### Create

```http
POST /api/{api_version}/{service_name}/{role}/{controller_name}
```

### Update

```http
PUT /api/{api_version}/{service_name}/{role}/{controller_name}/{id}
```

### Change Status

```http
POST /api/{api_version}/{service_name}/{role}/{controller_name}/{id}/change-status
```

### Soft Delete

```http
DELETE /api/{api_version}/{service_name}/{role}/{controller_name}/{id}/delete-soft
```

### Hard Delete

```http
DELETE /api/{api_version}/{service_name}/{role}/{controller_name}/{id}/delete-hard
```

### Restore

```http
POST /api/{api_version}/{service_name}/{role}/{controller_name}/{id}/restore
```
