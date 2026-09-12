# Feature: [Feature Name]

## 1. Metadata
- **Feature ID**: FEATURE-{DOMAIN}-{SEQUENCE}
- **Status**: Draft | Active | Completed
- **Created Date**: YYYY-MM-DD
- **Author**: [Author Name]

## 2. Business Objective
[Describe the business value and goal of this feature.]

## 3. Domain Entities
[Describe the core domain entities involved in this feature. Provide property types and descriptions.]

## 4. Permissions
[List the roles and JWT permissions required to access this feature.]

## 5. Business Rules
- **BR-001**: [Describe hard business constraints and rules]

## 6. Dependencies
- **Inbound**: [Events/API calls triggering this feature]
- **Outbound**: [Events emitted/APIs called by this feature]

## 7. Supported Operations
- [Operation 1]
- [Operation 2]

## 8. BDD Scenarios

### User Story
```gherkin
Feature: [Feature Name]

As a [Role]
I want [Goal]
So that [Benefit]
```

### Scenarios
```gherkin
Scenario: [Scenario Name]
Given [Precondition]
When [Action]
Then [Expected Result]

Scenario Outline: [Outline Name]
Given [Precondition]
When [Action with <variable>]
Then [Expected Result with <variable>]

Examples:
| variable |
|----------|
| value    |
```

## 9. Acceptance Criteria
- [ ] AC-001: Criterion 1 (Traceable to SCN-{DOMAIN}-001)

## 10. Traceability Matrix
| Requirement | Feature | Scenario | Task | Use Case | Repository | API | Test | Release |
|-------------|---------|----------|------|----------|------------|-----|------|---------|
| REQ-001 | FEATURE-001 | SCN-001 | TASK-001 | UC-001 | REPO-001 | API-001 | TEST-001 | REL-1.0.0 |

## Related

### Requirements

* REQ-{DOMAIN}-{SEQUENCE}

### Features

* FEATURE-{DOMAIN}-{SEQUENCE}

### Scenarios

* SCN-{DOMAIN}-{SEQUENCE}

### Tasks

* TASK-{DOMAIN}-{FEATURE-SEQUENCE}-{TASK-SEQUENCE}

### Use Cases

* UC-{DOMAIN}-{SEQUENCE}

### Repositories

* REPO-{DOMAIN}-{SEQUENCE}

### APIs

* API-{DOMAIN}-{SEQUENCE}

### Tests

* TEST-{DOMAIN}-{SEQUENCE}

### ADRs

* ADR-{SEQUENCE}

### Releases

* REL-{MAJOR.MINOR.PATCH}
