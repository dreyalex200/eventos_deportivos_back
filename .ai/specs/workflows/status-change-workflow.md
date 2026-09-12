# Workflow: Change Status (is_active)
**Status**: Active

**Related Rules**:
- [SDD Workflow](../../rules/workflows/sdd-workflow.md)
- [State Management](../../rules/workflows/state-management.md)

## Flow
1. **POST /change-status**: Receives `{"is_active": boolean}` in the body.
2. Invokes repository to update exclusively this field: `UPDATE table SET is_active = $1 WHERE id = $2`.
3. Returns 200 OK.
