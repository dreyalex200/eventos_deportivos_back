# HTTP Client Error Responses (4xx)
**Related**: [00-general.md](./00-general.md) | [01-success.md](./01-success.md) | [04-server-errors.md](./04-server-errors.md) | [security-rules.md](../security-rules.md)

---

## Purpose

Define the standard structure and behavior for all client error responses (4xx). These indicate that the client sent an invalid or unauthorized request. This standard is **language-agnostic** and applies to Go, Java, Kotlin, Dart/Flutter, and any other implementation language.

---

## 400 Bad Request

**Description**: The request contains validation errors or invalid parameters.

**When to use**:
- Missing or malformed query parameters
- Invalid field values (out of range, wrong format)
- Malformed JSON body

**Example — GET /api/v1/geo/client/reverse (invalid parameters)**
```json
{
  "status": "error",
  "message": "Request parameter errors",
  "errors": [
    {
      "field": "lat",
      "error": "Latitude must be between -90 and 90",
      "received_value": 100
    },
    {
      "field": "lon",
      "error": "Longitude must be between -180 and 180",
      "received_value": 200
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440007"
}
```

**Example — POST /api/v1/geo/protected/locations (required fields missing)**
```json
{
  "status": "error",
  "message": "Validation error",
  "errors": [
    {
      "field": "address",
      "error": "Address is required"
    },
    {
      "field": "city",
      "error": "City is required"
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440008"
}
```

---

## 401 Unauthorized

**Description**: Not authenticated. Token not provided, invalid, or expired.

**When to use**:
- No `Authorization` header present
- JWT token is malformed or has an invalid signature
- JWT token has expired

**Example — Missing token**
```json
{
  "status": "error",
  "message": "Authentication required",
  "errors": [
    {
      "code": "TOKEN_NOT_PROVIDED",
      "detail": "A JWT token is required in the Authorization header"
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440009"
}
```

**Example — Expired token**
```json
{
  "status": "error",
  "message": "Token expired",
  "errors": [
    {
      "code": "TOKEN_EXPIRED",
      "detail": "The token has expired. Please renew your session",
      "expiration": "2026-03-04T14:30:00Z"
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440010"
}
```

---

## 403 Forbidden

**Description**: Authenticated but insufficient permissions for the resource.

**When to use**:
- Valid token but the user's role does not have access
- RBAC permission check fails

**Example — DELETE /api/v1/geo/protected/locations/1**
```json
{
  "status": "error",
  "message": "Insufficient permissions for this operation",
  "errors": [
    {
      "code": "INSUFFICIENT_PERMISSIONS",
      "detail": "Administrator privileges are required",
      "current_role": "editor",
      "required_role": "admin"
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440011"
}
```

---

## 404 Not Found

**Description**: The requested resource does not exist.

**When to use**:
- Entity lookup by ID returns no results
- Endpoint does not exist
- Search returns no matching records

**Example — Resource by ID**
```json
{
  "status": "error",
  "message": "Resource not found",
  "errors": [
    {
      "code": "RESOURCE_NOT_FOUND",
      "detail": "Location with ID 9999 does not exist",
      "searched_id": 9999
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440012"
}
```

**Example — Reverse geocoding with no results**
```json
{
  "status": "error",
  "message": "No results found",
  "errors": [
    {
      "code": "NO_RESULTS",
      "detail": "No address found for the provided coordinates",
      "coordinates": {
        "latitude": -34.60,
        "longitude": -58.38
      }
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440013"
}
```

---

## 409 Conflict

**Description**: Conflict with the current state of the resource (e.g., duplicate).

**When to use**:
- Unique constraint violation (duplicate name, email, etc.)
- Optimistic locking conflict (concurrent modification)

**Example — POST /api/v1/geo/protected/locations (duplicate name)**
```json
{
  "status": "error",
  "message": "Conflict while creating the resource",
  "errors": [
    {
      "code": "DUPLICATE_RESOURCE",
      "detail": "A location with the name 'Central Park' already exists",
      "field": "name",
      "value": "Central Park",
      "existing_resource": {
        "id": 1
      }
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440014"
}
```

---

## 422 Unprocessable Entity

**Description**: The request is syntactically valid but cannot be processed due to business rule violations.

**When to use**:
- Data passes validation but violates business logic
- Cross-field validation failures

**Example — Coordinates mismatch with city**
```json
{
  "status": "error",
  "message": "Data does not comply with business rules",
  "errors": [
    {
      "code": "INVALID_COORDINATES",
      "detail": "The coordinates do not correspond to the specified city",
      "city": "Buenos Aires",
      "coordinates": {
        "latitude": -34.9011,
        "longitude": -56.1645
      }
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440015"
}
```

---

## 429 Too Many Requests

**Description**: Rate limit exceeded.

**When to use**:
- IP-based, user-based, or global rate limit exceeded
- Must include `Retry-After` header

**Example — Rate limit exceeded**
```json
{
  "status": "error",
  "message": "Rate limit exceeded",
  "errors": [
    {
      "code": "RATE_LIMIT_EXCEEDED",
      "detail": "You have exceeded the limit of 100 requests per minute",
      "limit": 100,
      "period": "1 minute",
      "retry_after_seconds": 45
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440016"
}
```

**Required Headers**:
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 45
Retry-After: 45
```

---

## Base Format Rules

All error responses **must** include:

| Field | Required | Description |
|-------|----------|-------------|
| `status` | ✅ | Always `"error"` |
| `message` | ✅ | General description of the error |
| `errors` | ✅ | Array with specific error details |
| `timestamp` | ✅ | ISO 8601 UTC |
| `request_id` | ✅ | UUID correlation ID matching `X-Request-ID` header |

### Error Object Structure

Each object in the `errors` array should follow this pattern:

```json
{
  "code": "UPPER_SNAKE_CASE_CODE",
  "detail": "Human-readable explanation",
  "field": "optional_field_name",
  "received_value": "optional_received_value"
}
```

---

## Agent Verification Checklist

- [ ] All 4xx responses include `status: "error"`, `message`, `errors` array, `timestamp`, and `request_id`.
- [ ] Error codes are in `UPPER_SNAKE_CASE` format.
- [ ] 400 responses include field-level validation details with `field` and `error` keys.
- [ ] 401 responses differentiate between `TOKEN_NOT_PROVIDED`, `TOKEN_EXPIRED`, and `TOKEN_INVALID`.
- [ ] 403 responses include `current_role` and `required_role` when applicable.
- [ ] 404 responses include the `searched_id` or search criteria.
- [ ] 409 responses reference the existing resource causing the conflict.
- [ ] 429 responses include `Retry-After` header and rate limit headers.
- [ ] No internal stack traces or infrastructure details are exposed in any error response.
