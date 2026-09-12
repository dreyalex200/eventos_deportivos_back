# Workflow: Full CRUD
**Status**: Active

**Related Rules**:
- [SDD Workflow](../../rules/workflows/sdd-workflow.md)
- [API Guidelines](../../rules/workflows/api-guidelines.md)

## Flow
1. **GET (List)**: Supports `limit`, `offset`, `trash` and filters like `q`. Returns a `meta` block with pagination.
2. **GET (ID)**: Fetches single record. Returns 404 (or 400 No Rows) if not found.
3. **POST (Create)**: Validates required JSON payload, checks for unique constraints, inserts record returning 201 Created.
4. **PUT (Update)**: Validates ID, updates record using current data ignoring primary keys. Returns 200 OK.
