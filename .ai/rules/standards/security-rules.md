# Security Rules — API Gateway & Services
**Related**: [jwt-structure.md](./jwt-structure.md) | [redis-keys.md](./redis-keys.md) | [http-responses/03-client-errors.md](./http-responses/03-client-errors.md) | [http-responses/04-server-errors.md](./http-responses/04-server-errors.md) | [environment-configuration.md](../specs/infrastructure/environment-configuration.md)

---

## Purpose

Define mandatory, non-negotiable security rules for all API Gateway and backend services. These rules apply regardless of implementation language (Go, Java, Kotlin, Dart/Flutter, etc.) and cover secrets management, input validation, HTTP security headers, rate limiting, authentication, logging, and more. Violations are treated as **critical bugs**.

---

## SR-01: Secrets Management

### ❌ NEVER

- Hardcode API keys, tokens, or passwords in source code
- Commit files with secrets (`.env`, `config*.yaml` with real secrets)
- Log sensitive information (JWT tokens, passwords, API keys)
- Include secrets in error responses
- Store secrets in unrestricted development environment variables

### ✅ ALWAYS

- Use environment variables for sensitive configuration
- Implement secret rotation in production
- Use vaults or secret managers in production (HashiCorp Vault, AWS Secrets Manager, GCP Secret Manager)
- Validate that secrets are not present in logs before writing
- Use default values only in development environments
- Strictly enforce the use of `<CHANGE_ME>` placeholders in `.env.example` templates for all sensitive keys (DB, JWT, Redis, APIs)

**Go Example**:
```go
// ✅ Correct — Use environment variables
jwtSecret := os.Getenv("JWT_SECRET")
if jwtSecret == "" {
    return fmt.Errorf("JWT_SECRET not set")
}

// ✅ Correct — Validate no secrets in logs
logger.Info("user authenticated", "user_id", userID, "method", "jwt")

// ❌ Incorrect — Hardcoded
jwtSecret := "my-super-secret-key-12345"

// ❌ Incorrect — Logging secrets
logger.Info("auth request", "token", token)
```

**Java/Kotlin Example**:
```kotlin
// ✅ Correct — Use environment variables
val jwtSecret = System.getenv("JWT_SECRET")
    ?: throw IllegalStateException("JWT_SECRET not set")

// ❌ Incorrect — Hardcoded
val jwtSecret = "my-super-secret-key-12345"
```

---

## SR-02: Input Validation

### ❌ NEVER

- Trust user input without validation
- Deserialize JSON without size limits
- Execute code based on user input
- Accept unknown fields in JSON requests

### ✅ ALWAYS

- Validate all inputs against defined schemas
- Limit request size (body, headers)
- Sanitize inputs to prevent injections (SQL, XSS)
- Reject unknown fields in request bodies

**Go Example**:
```go
// ✅ Correct — Complete validation
func validateCreateRouteRequest(w http.ResponseWriter, r *http.Request) (*RouteRequest, error) {
    body := http.MaxBytesReader(w, r.Body, 1048576) // 1MB limit
    defer body.Close()

    decoder := json.NewDecoder(body)
    decoder.DisallowUnknownFields()

    var req RouteRequest
    if err := decoder.Decode(&req); err != nil {
        return nil, fmt.Errorf("invalid JSON: %w", err)
    }

    if req.Path == "" {
        return nil, fmt.Errorf("path is required")
    }
    if !strings.HasPrefix(req.Path, "/") {
        return nil, fmt.Errorf("path must start with /")
    }

    return &req, nil
}
```

**Java/Kotlin Example**:
```kotlin
// ✅ Correct — Use Bean Validation
data class CreateRouteRequest(
    @field:NotBlank val path: String,
    @field:NotEmpty val methods: List<@Pattern(regexp = "GET|POST|PUT|DELETE|PATCH") String>,
    @field:NotBlank val serviceId: String
)
```

---

## SR-03: HTTP Security Headers

All responses **must** include these security headers:

| Header | Value | Purpose |
|--------|-------|---------|
| `X-Content-Type-Options` | `nosniff` | Prevent MIME-type sniffing |
| `X-Frame-Options` | `DENY` | Prevent clickjacking |
| `X-XSS-Protection` | `1; mode=block` | XSS filter |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | Control referrer information |
| `Content-Security-Policy` | `default-src 'self'` | Content security policy |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains; preload` | **Production only** — Force HTTPS |
| `Cache-Control` | `no-store, no-cache, must-revalidate, private` | **Admin routes only** — Prevent caching |

---

## SR-04: Rate Limiting & DDoS Protection

### Implement rate limiting in layers:

| Layer | Scope | Default Limit | Purpose |
|-------|-------|---------------|---------|
| 1. Global | Entire gateway | 10,000 req/min | Basic DDoS protection |
| 2. Per IP | IP address | 500 req/min | Prevent individual abuse |
| 3. Per User | Authenticated user | 1,000 req/min | Per-consumer control |
| 4. Per API Key | API key prefix | 5,000 req/min (test), 1,000 req/min (prod) | Per-integration limits |
| 5. Per Endpoint | Specific route | 5 req/min (login), 10 req/min (upload) | Sensitive endpoint protection |
| 6. Per Service | Backend service | 2,000 req/min (geo), 500 req/min (iam) | Backend service protection |

### Required Response Headers for Rate-Limited Endpoints

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 87
X-RateLimit-Reset: 45
Retry-After: 45
```

---

## SR-05: Authentication & Authorization

### JWT Tokens

- Token expiration **must** be ≤ 1 hour
- Use strong signing algorithms (HS256 minimum, RS256 preferred for production)
- Validate `exp`, `iat`, `sub`, and `jti` on every request
- See [jwt-structure.md](./jwt-structure.md) for full payload specification

### RBAC Authorization

- Validate user roles against route-level allowed roles
- Check granular permissions from `authorization.permissions` in the JWT
- Return `403 Forbidden` when authenticated but unauthorized

### Identity Propagation to Backend Services

When proxying requests to backend services, propagate identity via headers:

```http
X-User-ID: <user_id>
X-User-Email: <user_email>
X-User-Roles: <comma-separated-roles>
X-Request-ID: <request_uuid>
```

---

## SR-06: Logging & Sensitive Data

### ❌ NEVER log:

- Complete JWT tokens (only first/last characters if needed for debugging)
- API keys or passwords
- Credit card numbers
- Personally identifiable information (PII) without masking
- Complete authorization headers (`Authorization`, `X-API-Key`)

### ✅ ALWAYS redact automatically:

Sensitive fields that must be redacted in logs:
```
password, token, api_key, authorization,
credit_card, ssn, secret, jwt
```

### Secure Log Example

```json
{
  "level": "info",
  "timestamp": "2026-05-06T10:00:00Z",
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "method": "POST",
  "path": "/api/v1/iam/protected/auth/login",
  "status": 401,
  "client_ip": "192.168.1.100",
  "message": "authentication failed",
  "error": "invalid credentials"
}
```

---

## SR-07: Timeouts & Resource Limits

### Mandatory HTTP Server Configuration

| Parameter | Value | Purpose |
|-----------|-------|---------|
| Read Timeout | 5 seconds | Prevent slow-read attacks |
| Write Timeout | 10 seconds | Prevent hung connections |
| Idle Timeout | 120 seconds | Free idle resources |
| Header Read Timeout | 2 seconds | Protect against slow headers |
| Max Header Size | 1 MB | Prevent header flooding |
| Max Body Size | 1 MB (default) | Prevent oversized payloads |

---

## SR-08: Encryption & TLS

### Production Requirements

- **Minimum TLS version**: TLS 1.2
- **Maximum TLS version**: TLS 1.3
- **Preferred cipher suites**: AES-256-GCM, CHACHA20-POLY1305
- **Curve preferences**: P-256, X25519
- **HTTP → HTTPS redirect**: Mandatory in production

---

## SR-09: Injection Prevention

### SQL Injection

- **Always** use parameterized queries / prepared statements
- **Never** concatenate user input into SQL strings

```go
// ✅ Correct — Parameterized query
err := db.QueryRow("SELECT id FROM routes WHERE id = $1", id).Scan(&route.ID)

// ❌ Incorrect — String concatenation
query := "SELECT * FROM routes WHERE id = '" + id + "'"
```

```kotlin
// ✅ Correct — JPA parameterized query
@Query("SELECT r FROM Route r WHERE r.id = :id")
fun findById(@Param("id") id: String): Route?
```

### Command Injection

- **Never** include unsanitized user input in system commands
- Use argument arrays instead of shell string concatenation

---

## SR-10: Secure Error Handling

### ❌ NEVER expose:

- Stack traces to the client
- Internal database details (connection errors, SQL queries)
- Infrastructure information (internal IPs, file paths)
- Exact software versions

### ✅ Safe error mapping:

| Internal Error | HTTP Code | Client Message |
|----------------|-----------|----------------|
| DB connection error | 500 | Internal server error |
| Invalid JWT | 401 | Unauthorized |
| Rate limit exceeded | 429 | Too many requests. Try again later. |
| Route not found | 404 | Not found |
| Validation error | 400 | Bad request |

---

## SR-11: Dependency Security

### Regular verification:

- Run vulnerability scans on all dependencies
- Check for known CVEs before updating
- Integrate security audits in CI/CD pipeline

| Language | Audit Command |
|----------|---------------|
| Go | `govulncheck ./...` |
| Java/Kotlin | `./gradlew dependencyCheckAnalyze` (OWASP) |
| Dart/Flutter | `flutter pub audit` |
| Node.js | `npm audit` |

---

## SR-12: Audit & Compliance

### Record the following events for audit:

| Event Category | Events |
|---------------|--------|
| Resource Operations | `CREATE_*`, `UPDATE_*`, `DELETE_*` |
| Authentication | `LOGIN_SUCCESS`, `LOGIN_FAILURE`, `LOGOUT` |
| Configuration | `CONFIG_CHANGE` |
| Security | `PERMISSION_CHANGE`, `ROLE_CHANGE` |

### Audit Log Structure

```json
{
  "user_id": "admin@example.com",
  "action": "CREATE_ROUTE",
  "resource": "route",
  "target_id": "route-123",
  "old_data": null,
  "new_data": "{\"path\":\"/api/v1/test\",\"methods\":[\"GET\"]}",
  "ip": "192.168.1.100",
  "timestamp": "2026-05-06T10:00:00Z"
}
```

---

## Security Compliance Checklist

- [ ] All secrets are in environment variables — never hardcoded
- [ ] All inputs are validated (schemas, sizes, formats)
- [ ] HTTP security headers are present on all responses
- [ ] Rate limiting is configured in layers
- [ ] JWT tokens expire in ≤ 1 hour
- [ ] Logs redact sensitive information
- [ ] HTTP server timeouts are configured
- [ ] No SQL injection vulnerabilities (parameterized queries only)
- [ ] Error responses do not expose internal details
- [ ] Dependencies are scanned periodically for vulnerabilities
- [ ] Audit logs record all important changes
- [ ] TLS 1.2+ is enforced in production
- [ ] HTTPS redirect is active in production

---

## Agent Verification Checklist

- [ ] No hardcoded secrets found in generated code (`grep -rn "sk-\|Bearer \|password\s*=\|api_key\s*="` returns zero results).
- [ ] All environment-dependent values use environment variables or config injection.
- [ ] Input validation is present on all endpoints (size limits, schema validation, type checking).
- [ ] Security headers middleware is applied to all routes.
- [ ] Rate limiting is configured per the layer table above.
- [ ] JWT validation checks `exp`, `iat`, `sub`, `jti`, and `hashToken`.
- [ ] No sensitive data appears in log statements or error responses.
- [ ] All database queries use parameterized statements — no string concatenation.
- [ ] Error responses follow the envelope pattern and do not leak internal details.
- [ ] Dependency audit is integrated in the CI/CD pipeline.
