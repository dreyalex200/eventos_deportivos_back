# Environment Configuration Template
**Related**: [environment-configuration.md](../specs/infrastructure/environment-configuration.md)

---

## Purpose
This template provides the standardized boilerplate structure for generating `.env` and `.env.example` files across the ecosystem. It strictly enforces variable grouping and the `<CHANGE_ME>` security placeholder paradigm.

---

## .env.example Boilerplate

```env
# =====================================================
# Configuration: <SERVICE_NAME>
# =====================================================

# APLICACIÓN
APP_NAME=<service_name>
APP_ENV=development
APP_VERSION=1.0.0
APP_API_BASE_PATH=/api/v1/<service_name>

# SERVIDOR HTTP
SERVER_HOST=0.0.0.0
SERVER_PORT=<UNIQUE_ECOSYSTEM_PORT>
SERVER_CONTEXT_PATH=/
SERVER_READ_TIMEOUT=15s
SERVER_WRITE_TIMEOUT=15s
SERVER_IDLE_TIMEOUT=60s

# BASE DE DATOS
DATABASE_PROVIDER=postgres
DB_HOST=<CHANGE_ME>
DB_PORT=5432
DB_USER=<CHANGE_ME>
DB_PASSWORD=<CHANGE_ME>
DB_NAME=<database_name>
DB_SCHEMA=as_app_messaging
DB_DATABASE=<database_name>
DB_SSLMODE=require
DB_MAX_OPEN_CONNS=25
DB_MAX_IDLE_CONNS=10
DB_CONN_MAX_LIFETIME=5m


# RABBITMQ
RABBITMQ_ENABLED=true
RABBITMQ_HOST=<CHANGE_ME>
RABBITMQ_PORT=5672
RABBITMQ_USER=<CHANGE_ME>
RABBITMQ_PASSWORD=<CHANGE_ME>
RABBITMQ_VHOST=/

# SEGURIDAD JWT
JWT_SECRET=<CHANGE_ME>
JWT_EXPIRATION_HOURS=24
JWT_REFRESH_EXPIRATION=168
JWT_PERMISSIONS_KEY_PREFIX=<service_prefix>

APP_SECURITY_APP_TOKEN=<CHANGE_ME>
APP_SECURITY_APP_TOKEN_HEADER=X-App-Token
APP_SECURITY_APP_SCOPE_HEADER=X-Scope-Id

# CORS
CORS_ENABLED=true
CORS_ALLOWED_ORIGINS=*
CORS_ALLOW_CREDENTIALS=false

# REDIS & CACHÉ
REDIS_HOST=<CHANGE_ME>
REDIS_PORT=6379
REDIS_PASSWORD=<CHANGE_ME>
REDIS_DATABASE=0
CACHE_ENABLED=true
CACHE_TTL_MINUTES=5

# STORAGE (If Applicable)
APP_ASSETS_BASE_URL=<CHANGE_ME>
STORAGE_BASE_URL=<CHANGE_ME>
STORAGE_BUCKET=<CHANGE_ME>

# EMAIL & SMTP (If Applicable)
APP_EMAIL_FROM_EMAIL=no-reply@drey.com
APP_EMAIL_FROM_NAME=drey
SMTP_ENABLED=true
SENDGRID_ENABLED=false

MAIL_HOST=smtp.zoho.com
MAIL_PORT=587
MAIL_USERNAME=<CHANGE_ME>
MAIL_PASSWORD=<CHANGE_ME>
MAIL_SMTP_SSL_ENABLE=false

# INTEGRATIONS (If Applicable)
EXTERNAL_API_URL=<CHANGE_ME>
EXTERNAL_API_KEY=<CHANGE_ME>

# LOGS & OBSERVABILITY
LOG_LEVEL=info
LOG_FORMAT=json
LOGGING_LEVEL_ROOT=warn
LOGGING_LEVEL_COM_drey=info
```

---

## Generation Rules

1. **Required vs Optional**: Do not include blocks (e.g., Storage, External Integrations) if the service does not utilize them.
2. **Secrets**: Ensure `JWT_SECRET`, `DB_PASSWORD`, `REDIS_PASSWORD`, `MAIL_PASSWORD`, `RABBITMQ_PASSWORD`, and `EXTERNAL_API_KEY` are **always** initialized as `<CHANGE_ME>` in `.env.example`.
3. **Port Binding**: Assign the `<UNIQUE_ECOSYSTEM_PORT>` correctly during project generation.
