# Decision: Database Schema
**Status**: Accepted

**Related Rules**:
- [Dependency Rules](../../rules/architecture/dependency-rules.md)
- [SDD Process](../../rules/architecture/sdd.md)

- UUID is used in all Primary Keys.
- Unique constraints (`UNIQUE (country_id, code)`).
- Complete removal of the `sys_` prefix from geographic data tables to keep names clean.
