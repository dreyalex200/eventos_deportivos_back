# Requirement: Functional
**Status**: Active

**Related Rules**:
- [Dependency Rules](../../rules/architecture/dependency-rules.md)

## Details
The microservice must expose endpoints for geographic metadata CRUD operations.
Clients must be able to list countries, subdivisions, currencies, languages, and timezones.
Administrators must be able to create, update, disable, soft delete, hard delete, and restore all entities using a JWT authorization token.
