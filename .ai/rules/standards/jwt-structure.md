# JWT Token Structure — AuthGuard System
**Related**: [security-rules.md](./security-rules.md) | [http-responses/03-client-errors.md](./http-responses/03-client-errors.md)

---

## Purpose

Specify the JWT payload structure used by the AuthGuard system for authentication and authorization. This standard is **language-agnostic** and applies to any service that issues, validates, or consumes JWT tokens — whether implemented in Go, Java, Kotlin, Dart/Flutter, or any other language.

---

## Complete JWT Payload Example

```json
{
  "sub": "1",
  "jti": "6c693fdc-207c-4493-b5da-4921c91143be",
  "iat": 1761551584,
  "exp": 1761555184,
  "sid": "234af06b-2413-4549-b680-877ef2ad9532",
  "name": "Juan Guillermo Echeverri Tapias",
  "session": {
    "ip": "203.0.113.10",
    "device": "desktop",
    "agent": "Tester/1.0"
  },
  "authorization": {
    "hashToken": "1234567890",
    "role": "admin",
    "permissions": {
      "iam-api-uuid_app": [
        "IAM_APPLICATIONS_READ",
        "IAM_ASSIGNMENTS_WRITE",
        "IAM_MODULES_READ",
        "IAM_MODULES_WRITE",
        "IAM_PERMISSIONS_READ",
        "IAM_PERMISSIONS_WRITE",
        "IAM_ROLES_READ",
        "IAM_ROLES_WRITE",
        "IAM_USERS_READ",
        "IAM_USERS_WRITE"
      ],
      "geo-api-uuid_app": [
        "GEO_APPLICATIONS_READ",
        "GEO_ASSIGNMENTS_WRITE",
        "GEO_MODULES_READ",
        "GEO_MODULES_WRITE",
        "GEO_PERMISSIONS_READ",
        "GEO_PERMISSIONS_WRITE"
      ]
    }
  }
}
```

---

## Field Specification

### Section 1: Standard JWT Claims

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `sub` | `string` | ✅ Mandatory | **Subject** — Unique user identifier (user ID in the database) | `"1"` |
| `jti` | `string` (UUID) | ✅ Mandatory | **JWT ID** — Unique token identifier. Used for replay attack prevention and token revocation | `"6c693fdc-..."` |
| `iat` | `number` (Unix timestamp) | ✅ Mandatory | **Issued At** — Unix timestamp (seconds) when the token was issued | `1761551584` |
| `exp` | `number` (Unix timestamp) | ✅ Mandatory | **Expiration** — Unix timestamp (seconds) when the token expires | `1761555184` |
| `sid` | `string` (UUID) | ✅ Mandatory | **Session ID** — Unique session identifier. Represents a specific browser/device session | `"234af06b-..."` |

---

### Section 2: Session Information (`session`)

| Field | Type | Required | Description | Allowed Values |
|-------|------|----------|-------------|----------------|
| `session.ip` | `string` | ✅ Mandatory | Client IP address at authentication time. IPv4 or IPv6 | Any valid IP |
| `session.device` | `string` | ✅ Mandatory | Detected device type | `"desktop"`, `"mobile"`, `"tablet"`, `"iot"` |
| `session.agent` | `string` | ✅ Mandatory | Full User-Agent of the client application or browser | Any User-Agent string |

**Purpose**: This section enables:
- Access auditing by location/device
- Suspicious session change detection
- Usage pattern analysis

---

### Section 3: Authorization (`authorization`)

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `authorization.hashToken` | `string` | ✅ Mandatory | Unique hash for internal token validation. Serves as an additional integrity check | `"1234567890"` |
| `authorization.role` | `string` | ✅ Mandatory | User's primary role. Defines base access level | `"admin"`, `"user"`, `"moderator"`, `"guest"` |
| `authorization.permissions` | `object` | ✅ Mandatory | Granular permissions organized by application | See below |

---

### Permissions Structure (`authorization.permissions`)

```json
"permissions": {
  "<service-slug>-<application_uuid>": [
    "PERMISSION_CODE_1",
    "PERMISSION_CODE_2"
  ]
}
```

**Key format**: `<api_or_service>-<uuid_app>` where:
- `api_or_service`: Service name/slug (e.g., `iam-api`, `geo-api`)
- `uuid_app`: Application UUID (`application_id`)

**Value**: Array of one or more permission code strings associated with that application context.

### Standard Permission Codes

| Permission | Description |
|-----------|-------------|
| `READ` | View information |
| `WRITE` | Create or modify information |
| `DELETE` | Remove information |
| `EXPORT` | Export data |
| `IMPORT` | Import data |
| `GENERATE` | Generate new items (reports, documents) |
| `ADMIN` | Full administrative access |
| `EXECUTE` | Execute processes or actions |
| `APPROVE` | Approve requests or changes |

> **Convention**: Permission codes follow the pattern `{SERVICE}_{MODULE}_{ACTION}` in `UPPER_SNAKE_CASE` (e.g., `IAM_USERS_READ`, `GEO_MODULES_WRITE`).

---

## Timestamp Interpretation

```json
"iat": 1761551584,
"exp": 1761555184
```

| Timestamp | UTC Date/Time | Meaning |
|-----------|---------------|---------|
| `1761551584` | 2025-10-27 15:53:04 UTC | Token issuance time |
| `1761555184` | 2025-10-27 16:53:04 UTC | Token expiration (1 hour later) |

**Default token duration**: 3600 seconds (1 hour)

---

## UUID Identifiers

| UUID Field | Purpose |
|------------|---------|
| `jti` | Uniquely identifies THIS specific token |
| `sid` | Identifies the user's CURRENT session |
| `authorization.uuid` | Identifies the authorization CONFIGURATION |

---

## Required Validations

When receiving a JWT, the system **must** verify:

1. **Structure**: All mandatory fields are present
2. **Format**: UUIDs have valid format, timestamps are numbers
3. **Temporality**: `exp` is greater than current time
4. **Consistency**: `authorization.hashToken` matches expected value
5. **Permissions**: Permission structure is valid
6. **Application**: `appId` is in the list of allowed applications

---

## Important Considerations

### Payload Size
The complete payload is approximately **800 bytes**. The recommended limit is **4KB** to ensure compatibility with all servers and clients.

### Prohibited Content
This JWT must **NEVER** contain:
- Passwords or password hashes
- Third-party API tokens
- Credit card numbers
- Sensitive personal data (government IDs, full addresses)
- Private keys or secrets

### Permission Updates
Permissions in the JWT are static until the token expires. If a user's permissions are modified:
1. Wait for the token to expire, or
2. Issue a new token with updated permissions

---

## Agent Verification Checklist

- [ ] JWT payload contains all mandatory fields: `sub`, `jti`, `iat`, `exp`, `sid`, `session`, `authorization`.
- [ ] `session.device` is one of: `"desktop"`, `"mobile"`, `"tablet"`, `"iot"`.
- [ ] Permission codes follow `UPPER_SNAKE_CASE` format: `{SERVICE}_{MODULE}_{ACTION}`.
- [ ] Permission keys follow the pattern `<service-slug>-<application_uuid>`.
- [ ] Token expiration (`exp`) is set to 1 hour or less from issuance (`iat`).
- [ ] No passwords, API keys, credit card numbers, or private keys are included in the payload.
- [ ] Payload size remains under 4KB.
- [ ] All UUID fields (`jti`, `sid`) use valid UUID v4 format.
- [ ] `authorization.hashToken` is validated on every token verification.
