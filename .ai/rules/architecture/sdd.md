# Spec-Driven Development (SDD) — ADR Process
**Related**: [constitution.md](../constitution.md) | [sdd-workflow.md](../workflows/sdd-workflow.md)

---

## Purpose

Define when and how Architecture Decision Records (ADRs) are created, reviewed, and actioned. ADRs are the formal mechanism for evolving the architecture without violating the Constitution. Every meaningful architectural deviation begins as an ADR — never as uncommitted code.

---

## When to Create an ADR

An ADR is **mandatory** when any of the following conditions are met:

| Trigger | Example |
|---|---|
| Introduces a new package | Adding `MapStruct`, `Lombok`, `Testcontainers`, or any dependency not in `pom.xml`/`build.gradle` |
| Changes core patterns | Switching from pure Java DTOs to a framework-specific record mapping |
| Impacts multiple features | Changing how HTTP exceptions are globally handled, updating Spring Security config |
| Violates a Prime Directive | Bypassing Clean Architecture |
| Creates new shared infrastructure | Adding a new database type (e.g., MongoDB), Kafka |
| Deprecates an existing rule | Changing how Models map to Repositories |

An ADR is **not required** for:
- Implementing a new feature that follows existing patterns exactly.
- Adding a new Use Case, Entity, or Controller within an established feature.
- Fixing a bug without changing architecture.
- Updating package versions (semver patch or minor).

---

## ADR Template

Every ADR must use this exact template. Save to `.ai/specs/decisions/`.

```markdown
# ADR-{number}: {Title in Title Case}

**Date**: YYYY-MM-DD
**Status**: Proposed | Accepted | Deprecated | Superseded by ADR-{N}
**Author**: {name or "AI Agent"}
**Reviewer**: {architect name or "Pending"}

---

## Context
...
## Decision
...
## Consequences
...
## Implementation Notes
...
```

---

## How Agents Interact with ADRs

### Triggering a Review

When an AI agent encounters a situation requiring an ADR, it must:

1. **Stop implementation** of the affected component.
2. **Create the ADR draft** in `.ai/specs/decisions/` with `Status: Proposed`.
3. **Include in its response**: A clear statement that implementation is blocked pending ADR review.

```markdown
⚠️ ADR Required

I identified that implementing automated DTO mapping requires introducing a new
package (`MapStruct`) not currently defined in the architecture rules.

I have created the draft: `.ai/specs/decisions/2026-06-01-add-mapstruct.md`

Implementation is BLOCKED pending architect review of ADR-003.
```

### Reading Accepted ADRs

Before implementing any feature, agents **must**:

1. Check `.ai/specs/decisions/` for accepted ADRs related to the feature area.
2. Reference the ADR in generated code comments where the deviation occurs.

```java
// ✅ CORRECT: Reference accepted ADR in code when deviating
// ADR-004: Adopted MapStruct for DTO-to-Entity mapping.
// See: .ai/specs/decisions/2026-06-01-adopt-mapstruct.md
@Mapper(componentModel = "spring")
public interface UserMapper { ... }
```

## Agent Verification Checklist

- [ ] Every new `pom.xml`/`build.gradle` dependency has a `Proposed` ADR.
- [ ] Code that deviates from standard patterns due to an accepted ADR includes a comment referencing the ADR.
- [ ] Agents never self-approve ADRs.
