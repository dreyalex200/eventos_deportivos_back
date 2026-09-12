# Requirement: Technical
**Status**: Active

**Related Rules**:
- [Dependency Rules](../../rules/architecture/dependency-rules.md)
- [Environment Configuration](../infrastructure/environment-configuration.md)

## Details
- **Language**: Go 1.26
- **Database**: MySQL 9.7 LTS
- **Router**: go-chi
- **Architecture**: Hexagonal (Clean Architecture)
- **Deployment**: Dockerized containers inside Arcadion Ecosystem.
- **Environment Management**: Segregated via `.env` (runtime) and `.env.example` (template).
- **Configuration Validation**: Mandatory application panic on missing required variables.
- **Service Binding**: `SERVER_PORT` is mandatory and uniquely allocated per microservice.
