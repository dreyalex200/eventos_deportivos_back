# HTTP Success Responses (2xx)
**Related**: [00-general.md](./00-general.md) | [03-client-errors.md](./03-client-errors.md) | [04-server-errors.md](./04-server-errors.md)

---

## Purpose

Define the standard structure and behavior for all successful HTTP responses (2xx). Every success response must follow the envelope pattern specified in [00-general.md](./00-general.md). This standard is **language-agnostic** and applies to Go, Java, Kotlin, Dart/Flutter, and any other implementation language.

---

## 200 OK

**Description**: The request was processed successfully.

**When to use**:
- Successful `GET` requests returning data
- Successful `PUT`/`PATCH` requests returning the updated resource
- Successful `DELETE` requests when returning a confirmation body

**Example — GET /api/v1/geo/client/countries/CO**
```json
{
  "status": "success",
  "data": {
    "id": "CO",
    "name": "Colombia",
    "iso2_code": "CO",
    "iso3_code": "COL",
    "numeric_code": "170",
    "official_name": "Republic of Colombia",
    "currency": "COP",
    "language": "es",
    "timezone": "America/Bogota"
  },
  "message": "Country details retrieved successfully",
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 201 Created

**Description**: The resource was created successfully.

**When to use**:
- Successful `POST` requests that create a new resource
- Must include the created resource in the `data` field

**Example — POST /api/v1/geo/protected/countries**
```json
{
  "status": "success",
  "data": {
    "id": "AR",
    "name": "Argentina",
    "iso2_code": "AR",
    "iso3_code": "ARG",
    "numeric_code": "032",
    "currency": "ARS",
    "language": "es"
  },
  "message": "Country created successfully",
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440002"
}
```

---

## 202 Accepted

**Description**: The request was accepted for asynchronous processing.

**When to use**:
- Long-running operations (imports, batch processing, report generation)
- Must include a `job_id` and a `status_url` for polling

**Example — POST /api/v1/geo/protected/import**
```json
{
  "status": "success",
  "data": {
    "job_id": "geo-import-789",
    "job_status": "processing",
    "status_url": "/api/v1/geo/protected/import/status/geo-import-789"
  },
  "message": "Geographic data import started",
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440003"
}
```

---

## 204 No Content

**Description**: The request was successful but there is no content to return.

**When to use**:
- Successful `DELETE` requests with no response body
- Successful `PUT`/`PATCH` requests when the client does not need the updated resource

**Example — DELETE /api/v1/geo/protected/countries/AR**
```
Status: 204 No Content
Body: (empty)
Headers:
  - X-Request-ID: 550e8400-e29b-41d4-a716-446655440004
```

> **Note**: 204 responses must **not** include a body. The `X-Request-ID` header is still required for tracing.

---

## Base Format Rules

All success responses **must** include:

| Field | Required | Description |
|-------|----------|-------------|
| `status` | ✅ | Always `"success"` |
| `timestamp` | ✅ | ISO 8601 UTC |
| `request_id` | ✅ | UUID correlation ID matching `X-Request-ID` header |
| `message` | ⚠️ Recommended | Human-readable description of the operation result |
| `data` | ⚠️ Optional | Response payload — omit only for 204 responses |
| `meta` | ❌ Optional | Metadata such as pagination info, total counts, etc. |

### Pagination Metadata Example

```json
{
  "status": "success",
  "data": [ ... ],
  "meta": {
    "page": 1,
    "per_page": 20,
    "total_items": 156,
    "total_pages": 8
  },
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440099"
}
```

---

## Agent Verification Checklist

- [ ] All 2xx responses include `status: "success"`, `timestamp`, and `request_id`.
- [ ] 201 responses include the created resource in `data`.
- [ ] 202 responses include `job_id` and `status_url` for polling.
- [ ] 204 responses have an empty body and include `X-Request-ID` header.
- [ ] Paginated list endpoints include `meta` with pagination fields.
- [ ] No success response uses `status: "error"` or includes an `error` field.
