# HTTP Response Standards — General
**Related**: [01-success.md](./01-success.md) | [02-redirections.md](./02-redirections.md) | [03-client-errors.md](./03-client-errors.md) | [04-server-errors.md](./04-server-errors.md) | [security-rules.md](../security-rules.md)

---

## Purpose

Define the unified response format (Envelope Pattern) that all services must follow for every HTTP response — whether originating from the API Gateway itself or from any microservice behind the gateway. This standard is **language-agnostic** and applies equally to implementations in Go, Java, Kotlin, Dart/Flutter, or any other language.

---

## Base Response Format (Envelope Pattern)

All API responses **must** follow this structure:

```json
{
  "status": "success|error",
  "message": "Optional description",
  "data": { ... },
  "error": {
    "code": "ERROR_CODE",
    "details": "Detailed error description"
  },
  "timestamp": "2026-03-08T12:00:00Z",
  "request_id": "uuid"
}
```

### Field Definitions

| Field | Type | Required | Present When | Description |
|-------|------|----------|--------------|-------------|
| `status` | `string` | ✅ Always | Always | Operation outcome: `"success"` or `"error"` |
| `message` | `string` | ⚠️ Recommended | Always | Human-readable informational message |
| `data` | `object` | ❌ Optional | `status = "success"` | Response payload (entities, collections, metadata) |
| `error` | `object` | ❌ Optional | `status = "error"` | Structured error information |
| `error.code` | `string` | ✅ When error | `status = "error"` | Machine-readable error code in `UPPER_SNAKE_CASE` |
| `error.details` | `string` | ✅ When error | `status = "error"` | Detailed error description for developers |
| `timestamp` | `string` (ISO 8601) | ✅ Always | Always | Response timestamp in UTC (`RFC 3339`) |
| `request_id` | `string` (UUID) | ✅ Always | Always | Unique request identifier matching `X-Request-ID` header |

---

## HTTP Status Code Summary

| Code | Name | Usage |
|------|------|-------|
| **200** | OK | Successful operation (GET, PUT, DELETE) |
| **201** | Created | Resource created successfully (POST) |
| **202** | Accepted | Request accepted for asynchronous processing |
| **204** | No Content | Successful operation with no response body |
| **301** | Moved Permanently | Resource moved to a new permanent URL |
| **302** | Found | Temporary redirect |
| **304** | Not Modified | Resource unchanged since last request (caching) |
| **400** | Bad Request | Request validation or parameter errors |
| **401** | Unauthorized | Missing, invalid, or expired token |
| **403** | Forbidden | Valid token but insufficient permissions (RBAC) |
| **404** | Not Found | Requested resource does not exist |
| **409** | Conflict | Conflict with current state (e.g., duplicate resource) |
| **422** | Unprocessable Entity | Valid request but business rule violation |
| **429** | Too Many Requests | Rate limit exceeded |
| **500** | Internal Server Error | Unexpected server error |
| **502** | Bad Gateway | Communication error with backend service |
| **503** | Service Unavailable | Backend service is offline or under maintenance |
| **504** | Gateway Timeout | Backend service did not respond in time |

---

## Reference Implementation (Go)

### Response Struct

```go
type APIResponse struct {
    Status    string      `json:"status"`
    Message   string      `json:"message,omitempty"`
    Data      interface{} `json:"data,omitempty"`
    Error     *APIError   `json:"error,omitempty"`
    Timestamp string      `json:"timestamp"`
    RequestID string      `json:"request_id"`
}

type APIError struct {
    Code    string `json:"code"`
    Details string `json:"details"`
}
```

### Success Helper

```go
func SendSuccess(w http.ResponseWriter, r *http.Request, code int, data interface{}, message string) {
    resp := APIResponse{
        Status:    "success",
        Message:   message,
        Data:      data,
        Timestamp: time.Now().Format(time.RFC3339),
        RequestID: GetRequestID(r),
    }
    w.Header().Set("Content-Type", "application/json")
    w.WriteHeader(code)
    json.NewEncoder(w).Encode(resp)
}
```

### Error Helper

```go
func SendError(w http.ResponseWriter, r *http.Request, code int, errorCode, details string) {
    resp := APIResponse{
        Status:    "error",
        Error:     &APIError{Code: errorCode, Details: details},
        Timestamp: time.Now().Format(time.RFC3339),
        RequestID: GetRequestID(r),
    }
    w.Header().Set("Content-Type", "application/json")
    w.WriteHeader(code)
    json.NewEncoder(w).Encode(resp)
}
```

---

## Validation Criteria

1. **Consistency**: All responses without exception **must** use the envelope pattern.
2. **Request ID**: The `request_id` field **must** match the `X-Request-ID` header value.
3. **Error Codes**: Error codes (`error.code`) **must** be strings in `UPPER_SNAKE_CASE`.
4. **Security**: Never include stack traces or infrastructure details in `error.details` in production.
5. **Timestamps**: All timestamps **must** be in ISO 8601 / RFC 3339 format with UTC timezone.

---

## Agent Verification Checklist

- [ ] All API responses use the envelope pattern — no raw strings or unstructured JSON.
- [ ] `status` field is always present and is either `"success"` or `"error"`.
- [ ] `timestamp` is in ISO 8601 UTC format on every response.
- [ ] `request_id` is present on every response and matches `X-Request-ID` header.
- [ ] Error codes follow `UPPER_SNAKE_CASE` convention.
- [ ] No stack traces, internal IPs, or database details leak in production error responses.
- [ ] `data` field is only present on success responses; `error` field is only present on error responses.
