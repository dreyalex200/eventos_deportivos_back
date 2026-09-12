# Workflow: Soft Delete
**Status**: Active

**Related Rules**:
- [SDD Workflow](../../rules/workflows/sdd-workflow.md)
- [Database Standards](../../rules/standards/database-standards.md)

## Flow
1. **DELETE /delete-soft**: Executes `UPDATE table SET deleted_at = NOW() WHERE id = $1`.
2. The record becomes invisible to standard `GET` queries unless `trash=true` is used.
3. **POST /restore**: Validates if `deleted_at` is not null. If it is null, aborts. Otherwise executes `UPDATE table SET deleted_at = NULL WHERE id = $1`.
