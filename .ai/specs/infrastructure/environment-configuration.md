# Environment Configuration Management Standard
**Related**: [folder-structure.md](../../rules/architecture/folder-structure.md) | [hexagonal.md](../../rules/architecture/hexagonal.md) | [dependency-rules.md](../../rules/architecture/dependency-rules.md) | [security-rules.md](../../rules/standards/security-rules.md) | [technical.md](../requirements/technical.md) | [environment-template.md](../../templates/environment-template.md)

---

## Purpose
Define the overarching standards and responsibilities for managing environment configuration (`.env` and `.env.example`) within the ecosystem. This ensures all microservices enforce a secure, backward-compatible, and architecturally strict lifecycle for variables.

---

## Configuration Ownership

- **Layer Responsibility**: Environment configuration belongs purely to the **Infrastructure Layer**.
- **Domain Restriction**: Core Domain entities, business logic, and Application Use Cases are strictly forbidden from loading, reading, or processing `os.Getenv` or raw configuration variables.
- **Provider Mechanism**: Configurations must be loaded by Adapters (e.g., `config.go`) at startup and injected downward.

---

## File Responsibilities

### `.env`
- **Purpose**: Local runtime configuration representing the operational state.
- **Security**: Must **NEVER** be committed to the repository. It contains real DB credentials, live API keys, and secret JWT signatures.
- **Location**: Root directory of the service.

### `.env.example`
- **Purpose**: Version-controlled structural template documenting the full environment schema.
- **Security**: Must **NEVER** contain sensitive data or production values.
- **Placeholders**: All sensitive variables must be set to `<CHANGE_ME>`.
- **Location**: Root directory of the service.

---

## Naming Conventions & Required Categories

All environment variables must be uppercase, snake_cased, and grouped by explicit responsibilities.

### Mandatory Groups:
1. **Application**: `APP_NAME`, `APP_ENV`, `APP_VERSION`
2. **Server**: `SERVER_HOST`, `SERVER_PORT`
3. **Database**: `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`, `DB_SCHEMA`, `DB_SSLMODE`
4. **Security / JWT**: `JWT_SECRET`, `JWT_EXPIRATION_HOURS`

### Conditional Groups:
5. **Cache / Redis**: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
6. **External APIs**: `{SERVICE}_API_URL`, `{SERVICE}_API_KEY`
7. **Storage**: `STORAGE_BASE_URL`, `STORAGE_BUCKET`
8. **Observability**: `LOG_LEVEL`, `LOG_FORMAT`

*(Refer to [environment-template.md](../../templates/environment-template.md) for full scaffolding).*

---

## Port Allocation Rules (`SERVER_PORT`)

1. **Mandatory**: Every microservice requires a unique `SERVER_PORT`.
2. **Conflict Avoidance**: Before allocating a port, the Architect or integration agent must analyze existing ecosystem services and ensure no overlapping assignments.
3. **Immutability**: Once a port is assigned to a production service, it must not be re-assigned or reused.

---

## Lifecycle Validation

All changes to `.env` variables invoke SDD workflow validations:
- **Feature SDD**: Modifying infrastructure introduces an update to `.env.example`.
- **Integrations SDD**: Adding third-party endpoints necessitates credential placeholders.
- **Release SDD**: Deployments strictly block on missing keys in `.env.example`.
