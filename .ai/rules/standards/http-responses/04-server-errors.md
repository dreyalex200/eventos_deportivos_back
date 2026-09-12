# HTTP Server Error Responses (5xx)
**Related**: [00-general.md](./00-general.md) | [03-client-errors.md](./03-client-errors.md) | [security-rules.md](../security-rules.md)

---

## Purpose

Define the standard structure and behavior for all server error responses (5xx). These indicate failures on the server side that are not caused by the client. This standard is **language-agnostic** and applies to Go, Java, Kotlin, Dart/Flutter, and any other implementation language.

> **Critical Security Rule**: Server error responses must **never** expose stack traces, internal IP addresses, database connection details, file paths, or any infrastructure information. Log full details internally; return only safe, generic messages to the client.

---

## 500 Internal Server Error

**Description**: An unexpected error occurred on the server.

**When to use**:
- Unhandled exceptions
- Database errors (connection failures, query errors)
- Unexpected application state

**Example — Database error**
```json
{
  "status": "error",
  "message": "Internal server error",
  "errors": [
    {
      "code": "DATABASE_ERROR",
      "detail": "The operation could not be completed due to a database error",
      "transient": true,
      "recommendation": "Please try again in a few minutes"
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440017"
}
```

**Example — Database timeout**
```json
{
  "status": "error",
  "message": "Error processing the request",
  "errors": [
    {
      "code": "DATABASE_TIMEOUT",
      "detail": "The operation exceeded the time limit",
      "timeout_seconds": 30
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440018"
}
```

---

## 502 Bad Gateway

**Description**: The API gateway received an invalid response from an upstream service.

**When to use**:
- Backend microservice did not respond
- Communication error with an external service
- Invalid response from upstream

**Example — External service unreachable**
```json
{
  "status": "error",
  "message": "Communication error with external service",
  "errors": [
    {
      "code": "SERVICE_UNAVAILABLE",
      "detail": "The geocoding service is temporarily unavailable",
      "service": "geo-external",
      "retry_after_seconds": 5
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440019"
}
```

---

## 503 Service Unavailable

**Description**: The service is temporarily unavailable.

**When to use**:
- Scheduled maintenance
- System overload
- Database down
- Circuit breaker open

**Example — Scheduled maintenance**
```json
{
  "status": "error",
  "message": "Service temporarily unavailable",
  "errors": [
    {
      "code": "SCHEDULED_MAINTENANCE",
      "detail": "The service is under scheduled maintenance",
      "start_time": "2026-03-04T15:00:00Z",
      "estimated_end_time": "2026-03-04T17:00:00Z",
      "maintenance_window": "2 hours"
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440020"
}
```

**Required Headers**:
```http
Retry-After: 5400
```

---

## 504 Gateway Timeout

**Description**: The upstream service did not respond within the time limit.

**When to use**:
- Backend service is too slow to respond
- Complex queries exceeding execution time
- Network latency with upstream services

**Example — Complex query timeout**
```json
{
  "status": "error",
  "message": "Gateway timeout",
  "errors": [
    {
      "code": "QUERY_TIMEOUT",
      "detail": "The query exceeded the maximum execution time",
      "timeout_seconds": 10,
      "complexity": "high",
      "recommendation": "Try a more specific search with fewer parameters"
    }
  ],
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440021"
}
```

---

## Internal Logging Requirements

While responses to the client remain generic, the server **must** log full details internally:

```json
{
  "level": "error",
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440017",
  "method": "GET",
  "path": "/api/v1/geo/client/search",
  "status": 500,
  "error": "pq: connection refused to 10.0.0.5:5432",
  "stack_trace": "...",
  "client_ip": "203.0.113.10",
  "user_id": "user-123",
  "duration_ms": 1523
}
```

> **Security**: Internal logs contain full error details. These must **never** be returned to the client.

---

## Base Format Rules

All 5xx responses **must** include:

| Field | Required | Description |
|-------|----------|-------------|
| `status` | ✅ | Always `"error"` |
| `message` | ✅ | Generic, safe error description |
| `errors` | ✅ | Array with safe error details (no internals) |
| `timestamp` | ✅ | ISO 8601 UTC |
| `request_id` | ✅ | UUID correlation ID for client-side tracing |

### Common Error Codes (5xx)

| Code | Description |
|------|-------------|
| `INTERNAL_ERROR` | Generic unclassified server error |
| `DATABASE_ERROR` | Database operation failure |
| `DATABASE_TIMEOUT` | Database query timeout |
| `SERVICE_UNAVAILABLE` | Upstream service unreachable |
| `SCHEDULED_MAINTENANCE` | Planned maintenance window |
| `QUERY_TIMEOUT` | Query exceeded execution time |
| `CIRCUIT_BREAKER_OPEN` | Circuit breaker is open for an upstream service |

---

## Agent Verification Checklist

- [ ] All 5xx responses include `status: "error"`, `message`, `errors`, `timestamp`, and `request_id`.
- [ ] No stack traces, internal IPs, database connection strings, or file paths appear in any 5xx response.
- [ ] Error codes follow `UPPER_SNAKE_CASE` format.
- [ ] 503 responses include `Retry-After` header when maintenance window is known.
- [ ] Full error details (stack traces, internal errors) are logged server-side only.
- [ ] `request_id` is present for end-to-end tracing between client and internal logs.
- [ ] Transient errors include `recommendation` field guiding the client on retry behavior.
