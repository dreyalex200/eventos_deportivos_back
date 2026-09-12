# Project Constitution — Go Microservices
**Status**: Active | **Version**: 2.0.0 | **Last Updated**: 2026-06-04

> This document is the supreme governing ruleset for all Go backend microservices in the ecosystem.
> All other rules, guidelines, and agent instructions derive their authority from this Constitution.
> No rule in any other document may contradict the Prime Directives defined herein.

---

## Preamble

This constitution establishes the inviolable principles that govern every line of code, every architectural decision, and every workflow in Go microservice projects (`api_geo`, `api_iam`, `api_entity`, etc.). It exists to ensure long-term maintainability, predictability, and quality above all other concerns—including speed of delivery.

Any agent or developer who encounters a conflict between delivering quickly and following these principles **must** follow these principles and escalate the conflict via an Architecture Decision Record (ADR).

---

## Prime Directives

These five directives are the highest-priority rules in the entire system. They override all preferences, deadlines, and convenience arguments.

### Prime Directive 1: Clean Architecture Over Quick Fixes

> **"The correct structure is always cheaper in the long run."**

- Every feature **must** respect the Hexagonal Architecture layer separation: Adapters (Handlers) → Core (Services) → Core (Ports/Domain) ← Adapters (Repositories).
- No shortcut bypasses a layer boundary. If a handler needs data, it goes through a Service. If a Service needs data, it goes through a Repository Port. If a Port needs implementation, it lives in a Repository Adapter.
- "Temporary" violations that break layering **do not exist**. A violation is a bug, not a shortcut.

**Consequence of violation**: The code is rejected. A refactor task is created and assigned before any new work continues on that feature.

---

### Prime Directive 2: Testability Over Convenience

> **"If it cannot be tested in isolation, it is incorrectly designed."**

- Every component with business logic **must** be unit-testable without a real database, network, or external service.
- Dependencies that prevent isolation (database connections, HTTP clients) **must** be injected via interfaces (ports)—never instantiated inside business logic.
- Test coverage for Core (domain + services) layers **must** exceed 80%. No exceptions.

**Consequence of violation**: The PR is rejected. Tests must be written before the PR can be re-submitted.

---

### Prime Directive 3: Semantic Code Over Magic Strings

> **"Every value must have a name. Every name must have a meaning."**

- No raw strings for API endpoints, configuration keys, error codes, or semantic identifiers.
- All such values **must** be declared as typed constants in their corresponding module (e.g., `ErrorCodes`, `ConfigKeys`).
- Error codes **must** follow `UPPER_SNAKE_CASE` convention.

**Consequence of violation**: The linter will catch it. If it passes the linter, a code reviewer must flag it, and the code must be corrected before merge.

---

### Prime Directive 4: Spec Compliance Over Autonomy

> **"Agents must follow specifications exactly. Humans must follow specifications unless they write a new one."**

- AI agents **must not** deviate from the rules in `.ai/rules/` under any circumstances, even if they believe a different approach is superior.
- If an agent identifies a superior approach, it **must** flag the spec for review and generate an ADR draft. It does **not** implement the new approach until the ADR is accepted.
- Developers may propose changes to specifications via the ADR process, but **not** unilaterally override them in code.

**Consequence of violation**: Agent output is discarded. Developer code requires architect review and an ADR before the approach can be used.

---

### Prime Directive 5: Known Patterns Over Clever Innovations

> **"Boring, predictable code is a feature, not a limitation."**

- Use the established patterns defined in `.ai/rules/` for every situation they cover.
- New patterns are introduced only via the ADR process, not by individual developer preference.
- "Creative" solutions that deviate from established patterns without an ADR are a liability, not an asset.

**Consequence of violation**: Code review rejection. The code must be rewritten using established patterns.

---

## Governing Rules Hierarchy

When a conflict arises between documents, apply this hierarchy (highest to lowest priority):

```
1. constitution.md                  ← You are here. Supreme authority.
2. .ai/specs/decisions/*.md         ← Accepted ADRs (project-specific overrides)
3. .ai/rules/architecture/*.md      ← Core structural rules (hexagonal, clean arch, dependencies)
4. .ai/rules/standards/*.md         ← Cross-language standards (HTTP, JWT, Redis, Security, Code Style)
5. .ai/rules/workflows/*.md         ← Process and workflow rules
6. Team consensus                   ← Only when no rule covers the case
```

---

## Violation Escalation Protocol

When a Prime Directive violation is detected:

1. **Stop**: Do not merge, deploy, or build upon the violating code.
2. **Document**: Create a draft ADR in `.ai/specs/decisions/` describing the violation, its context, and proposed resolution.
3. **Notify**: Assign the ADR to the lead architect or technical owner.
4. **Wait**: No work dependent on the violating component proceeds until the ADR is resolved.
5. **Resolve**: Either fix the code to comply, or formally update the spec via the ADR process.

```
Violation Detected
      │
      ▼
Create ADR Draft (.ai/specs/decisions/YYYY-MM-DD-violation-description.md)
      │
      ▼
Architect Review (within 1 business day for blockers)
      │
      ├── ADR Rejected → Fix the code to comply
      └── ADR Accepted → Update the spec, then implement
```

---

## Non-Negotiable Technical Baselines

These technical constraints are not architectural preferences—they are fixed requirements:

| Constraint | Value | Rationale |
|---|---|---|
| Primary Language | Go (1.22+) | Performance, concurrency, static typing |
| Architecture | Hexagonal (Ports & Adapters) | Defined in `architecture/` rules |
| Core Structure | `internal/core/` (domain + ports + services) | Clean separation of business logic |
| Handler Separation | `client/` (public) + `protected/` (JWT-protected) | Mandatory security boundary |
| Database | MySQL 9.7 LTS | ACID compliance, JSON support, maturity |
| HTTP Router | go-chi/chi or gorilla/mux | Middleware support, path parameters |
| JWT Library | golang-jwt/jwt/v5 | Standards-compliant, maintained |
| Logging | Structured JSON (slog or zerolog) | Observability, machine-parseable |
| Configuration | Environment variables + `.env` per environment | 12-factor app compliance |
| Error Handling | Explicit error returns | No panics in handler or library code |
| API Response Format | Envelope Pattern | Defined in `standards/http-responses/` |
| Deployment | Docker + CapRover | Consistent environments |
| Linter | golangci-lint | Code quality gate |

---

## What Agents Are Authorized to Do

AI coding agents operating in this project are authorized to:

- ✅ Generate code that strictly follows all `.ai/rules/` specifications.
- ✅ Create new files and directories following the hexagonal folder structure.
- ✅ Write unit and integration tests.
- ✅ Update domain models, ports, services, and adapter implementations.
- ✅ Draft ADRs for review (but not accept them autonomously).
- ✅ Flag inconsistencies between specifications and request human review.
- ✅ Create and update SQL migrations and seeders.

AI coding agents are **explicitly forbidden** from:

- ❌ Accepting their own ADR drafts.
- ❌ Bypassing any Prime Directive for any reason, including time pressure.
- ❌ Introducing new packages without an accepted ADR.
- ❌ Writing code that cannot be unit tested.
- ❌ Placing infrastructure imports inside `internal/core/`.
- ❌ Generating "placeholder" architecture that violates layer boundaries.
- ❌ Using `panic()` in handler or library code.
- ❌ Hardcoding secrets, API keys, or environment-specific values.
- ❌ Putting `json`, `gorm`, or `db` struct tags on domain models.
- ❌ Mixing client and protected endpoints in the same handler or routes file.

---

## Agent Verification Checklist

Before submitting any output, an agent must verify:

- [ ] **PD1**: Does all generated code respect the hexagonal layer boundaries (no Core importing Adapters)?
- [ ] **PD2**: Is every component with business logic unit-testable without a database or network?
- [ ] **PD3**: Are all endpoints, error codes, and config keys declared as typed constants?
- [ ] **PD4**: Does the generated code exactly follow the applicable `.ai/rules/` spec? No deviations?
- [ ] **PD5**: Does the generated code use established patterns from `.ai/rules/`? No novel inventions?
- [ ] **Hierarchy**: If there is a rule conflict, was the correct priority hierarchy applied?
- [ ] **ADR Required**: Does any decision require an ADR? If yes, has the draft been created?
- [ ] **Baselines**: Does the code use the mandated technical stack (Go, MySQL 9.7 LTS, chi/mux)?
- [ ] **Client/Admin**: Are client and protected endpoints properly separated with JWT on protected?
- [ ] **Coverage**: Are tests written or planned to achieve >80% coverage for Core layers?
- [ ] **Security**: Does the code follow all security rules defined in `standards/security-rules.md`?
- [ ] **HTTP Responses**: Do all API responses follow the envelope pattern from `standards/http-responses/`?
- [ ] **No Violations**: Can the agent certify that no Prime Directive is violated in the output?
