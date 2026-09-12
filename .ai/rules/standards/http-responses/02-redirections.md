# HTTP Redirection Responses (3xx)
**Related**: [00-general.md](./00-general.md) | [01-success.md](./01-success.md) | [03-client-errors.md](./03-client-errors.md)

---

## Purpose

Define the standard structure and behavior for HTTP redirection responses (3xx). These responses indicate that the client must take additional action to complete the request. This standard is **language-agnostic** and applies to Go, Java, Kotlin, Dart/Flutter, and any other implementation language.

---

## 301 Moved Permanently

**Description**: The resource has been permanently moved to a new URL. Clients must update their references.

**When to use**:
- API version deprecation — endpoint moved to a new version
- Permanent URL restructuring

**Example — GET /api/v1/geo/search (deprecated endpoint)**
```json
{
  "status": "success",
  "message": "This endpoint has been permanently moved",
  "data": {
    "new_location": "/api/v2/geo/client/search",
    "reason": "API v2 upgrade",
    "deprecated_since": "2026-03-01",
    "sunset_date": "2026-06-01"
  },
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440005"
}
```

**Required Headers**:
```http
Location: /api/v2/geo/client/search
Sunset: Sat, 01 Jun 2026 00:00:00 GMT
Deprecation: Mon, 01 Mar 2026 00:00:00 GMT
```

---

## 302 Found

**Description**: Temporary redirect. The resource is temporarily available at another URL.

**When to use**:
- Scheduled maintenance — temporary fallback endpoint
- Temporary load balancing or failover

**Example — GET /api/v1/geo/client/search (under maintenance)**
```json
{
  "status": "success",
  "message": "Service under temporary maintenance",
  "data": {
    "temporary_location": "/api/v1/geo/client/search-backup",
    "estimated_return_time": "2026-03-04T18:00:00Z",
    "reason": "Scheduled maintenance"
  },
  "timestamp": "2026-03-04T15:30:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440006"
}
```

**Required Headers**:
```http
Location: /api/v1/geo/client/search-backup
```

---

## 304 Not Modified

**Description**: The resource has not been modified since the last request. Used for caching.

**When to use**:
- Client sends `If-None-Match` (ETag) or `If-Modified-Since` headers
- The resource has not changed — no body is sent

**Example — GET /api/v1/geo/client/search?q=park (with cache)**
```
Status: 304 Not Modified
Headers:
  - ETag: "33a64df551425fcc55e4d42a148795d9f25f89d4"
  - Cache-Control: max-age=3600
  - X-Request-ID: 550e8400-e29b-41d4-a716-446655440007
Body: (empty)
```

> **Note**: 304 responses must **not** include a body.

---

## Base Format Rules

Redirection responses **must** include:

| Field | Required | Description |
|-------|----------|-------------|
| `status` | ✅ | `"success"` — the redirection operation itself is successful |
| `timestamp` | ✅ | ISO 8601 UTC |
| `request_id` | ✅ | UUID correlation ID matching `X-Request-ID` header |
| `message` | ✅ | Explanation of the redirection reason |
| `data` | ⚠️ When body present | Information about the new location |
| `Location` header | ✅ | Target URL (required by HTTP spec for 301/302) |

---

## Agent Verification Checklist

- [ ] All 3xx responses include the `Location` header (except 304).
- [ ] 301 responses include `deprecated_since` and `sunset_date` in the body for API deprecations.
- [ ] 302 responses include `estimated_return_time` for maintenance redirects.
- [ ] 304 responses include `ETag` and `Cache-Control` headers, with an empty body.
- [ ] `X-Request-ID` header is present on all redirection responses.
- [ ] Redirection responses use `status: "success"` — the redirect itself is a successful operation.
