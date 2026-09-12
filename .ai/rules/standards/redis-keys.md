# Redis Key Structure Standard
**Related**: [security-rules.md](./security-rules.md) | [http-responses/00-general.md](./http-responses/00-general.md)

---

## Purpose

Define the standard naming convention and structure for all Redis keys used across services. This ensures consistency, predictability, and easy debugging across all environments. This standard is **language-agnostic** and applies to any service that interacts with Redis — whether implemented in Go, Java, Kotlin, Dart/Flutter, or any other language.

---

## Key Naming Convention

All Redis keys **must** follow this pattern:

```
<namespace>:<category>:<identifier>
```

- **namespace**: Logical grouping (e.g., `route`, `rate`, `cache`, `stats`)
- **category**: Sub-classification within the namespace
- **identifier**: Specific key identifier (ID, path, IP, etc.)

> **Rules**:
> - Use lowercase only
> - Use colon (`:`) as the separator
> - Never include spaces or special characters beyond colons
> - Keep keys as short as possible while remaining descriptive

---

## Standard Key Definitions

### Routes

```redis
route:all                        -> JSON list of all active routes
route:{id}                       -> Specific route (JSON)
route:path:{path}                -> Route ID by path
route:service:{service_id}       -> Set of route_ids by service
route:version                    -> Integer that increments on every change
```

| Key Pattern | Value Type | TTL | Description |
|-------------|-----------|-----|-------------|
| `route:all` | JSON string | No TTL (invalidated on change) | Complete list of active routes |
| `route:{id}` | JSON string | No TTL | Single route configuration |
| `route:path:{path}` | String (ID) | No TTL | Reverse lookup: path → route ID |
| `route:service:{service_id}` | Redis Set | No TTL | All route IDs for a given service |
| `route:version` | Integer | No TTL | Cache-busting version counter |

---

### Rate Limiting

```redis
rate:ip:{ip}:{endpoint}          -> Counter per IP
rate:user:{user_id}:{endpoint}   -> Counter per user
rate:global:{endpoint}           -> Global counter
```

| Key Pattern | Value Type | TTL | Description |
|-------------|-----------|-----|-------------|
| `rate:ip:{ip}:{endpoint}` | Integer (counter) | Window period (e.g., 60s) | Request count per IP per endpoint |
| `rate:user:{user_id}:{endpoint}` | Integer (counter) | Window period | Request count per authenticated user |
| `rate:global:{endpoint}` | Integer (counter) | Window period | Total request count per endpoint |

> **Note**: Use `INCR` + `EXPIRE` (or `MULTI`/`EXEC`) to atomically increment and set TTL on first access.

---

### Response Cache

```redis
cache:resp:{method}:{path}:{hash(headers)} -> Cached response
```

| Key Pattern | Value Type | TTL | Description |
|-------------|-----------|-----|-------------|
| `cache:resp:{method}:{path}:{hash}` | JSON string | Route-specific (e.g., 300s) | Cached HTTP response body + headers |

**Hash computation**: The `{hash}` component is a hash of relevant request headers (e.g., `Accept`, `Authorization`) to differentiate cached responses per user/format.

---

### Statistics

```redis
stats:hits:total                 -> Total cache hit counter
stats:misses:total               -> Total cache miss counter
```

| Key Pattern | Value Type | TTL | Description |
|-------------|-----------|-----|-------------|
| `stats:hits:total` | Integer (counter) | No TTL | Total number of cache hits |
| `stats:misses:total` | Integer (counter) | No TTL | Total number of cache misses |

---

## Examples by Service

When services need their own Redis keys, prefix with the service name:

```redis
# IAM Service
iam:session:{session_id}         -> Session data (JSON)
iam:blacklist:{jti}              -> Revoked JWT token ID
iam:user:{user_id}:permissions   -> Cached user permissions

# Geo Service
geo:cache:country:{iso2}         -> Cached country data
geo:cache:search:{hash}          -> Cached search results
```

---

## Key Lifecycle Rules

1. **Rate limiting keys**: Must use TTL equal to the rate limiting window.
2. **Cache keys**: Must use TTL appropriate for the data freshness requirements.
3. **Route keys**: No TTL — invalidated explicitly when routes change (increment `route:version`).
4. **Session keys**: Must use TTL equal to session expiration time.
5. **Blacklist keys**: Must use TTL equal to the remaining token lifetime.

---

## Agent Verification Checklist

- [ ] All Redis keys follow the `<namespace>:<category>:<identifier>` pattern.
- [ ] Keys use lowercase only with colon separators — no spaces or special characters.
- [ ] Rate limiting keys have TTL set equal to the rate window period.
- [ ] Cache keys have appropriate TTL configured.
- [ ] Route keys are invalidated explicitly (no TTL-based expiration).
- [ ] Service-specific keys are prefixed with the service name (e.g., `iam:`, `geo:`).
- [ ] No sensitive data (passwords, tokens, PII) is stored as plain text in Redis values.
- [ ] `INCR` + `EXPIRE` are used atomically for rate limiting counters.
