# Feature: Languages

## 1. Metadata
- **Requirement ID**: REQ-GEO-LANGUAGE-001
- **Feature ID**: FEATURE-GEO-LANGUAGE-001
- **Domain**: Geo
- **Module**: Languages
- **Version**: 1.0.0
- **Status**: Active
- **Created Date**: 2026-06-15
- **Author**: Antigravity SDD Agent

## 2. Business Objective
Provide a centralized catalog of languages.

## 3. Domain Entities
**Language**
| Field | Type |
|-------|------|
| ID | UUID |
| IsActive | Boolean |
| CreatedAt | Timestamp |
| UpdatedAt | Timestamp |
| DeletedAt | Timestamp |
*Additional Fields*: Iso639_1 (String), Iso639_2 (String), Name (String), NativeName (String)

## 4. Permissions
- **Read**: `API_GEO_LANGUAGES_READ`
- **Write**: `API_GEO_LANGUAGES_WRITE`

## 5. Business Rules
- **BR-001**: ISO 639-1 code must be unique.
- **BR-002**: ISO 639-2 code must be unique.


## 6. Dependencies
- **Inbound**: Admin API HTTP calls
- **Outbound**: MySQL 9.7 LTS persistence

## 7. Supported Operations
### Administration
- List Languages
- Get Language By ID
- Create Language
- Update Language
- Change Status
- Soft Delete
- Hard Delete
- Restore

## 8. BDD Scenarios

### User Story
```gherkin
Feature: Languages Management

As an administrator
I want to manage Languages
So that provide a centralized catalog of languages.
```

### Scenarios
```gherkin
Scenario: Create Language Successfully
Given an administrator with permission API_GEO_LANGUAGES_WRITE
And a valid payload
When the administrator creates a Language
Then the Language is persisted
And HTTP 201 is returned
```

## 9. Acceptance Criteria
- [ ] AC-001: Creation succeeds with valid data (Traceable to SCN-GEO-LANGUAGE-001)
- [ ] AC-002: Validation rejects invalid payloads

## 10. Traceability Matrix
| Requirement | Feature | Scenario | Task | Use Case | Repository | API | Test | Release |
|-------------|---------|----------|------|----------|------------|-----|------|---------|
| REQ-GEO-LANGUAGE-001 | FEATURE-GEO-LANGUAGE-001 | SCN-GEO-LANGUAGE-001 | TASK-GEO-LANGUAGE-001-001 | UC-GEO-LANGUAGE-001 | REPO-GEO-LANGUAGE-001 | API-GEO-LANGUAGE-001 | TEST-GEO-LANGUAGE-001 | REL-1.0.0 |

## 11. Related Artifacts
### Requirements
* REQ-GEO-LANGUAGE-001

### Scenarios
* SCN-GEO-LANGUAGE-001

### Use Cases
* UC-GEO-LANGUAGE-001

### Repositories
* REPO-GEO-LANGUAGE-001

### APIs
* API-GEO-LANGUAGE-001

### Tests
* TEST-GEO-LANGUAGE-001

### Releases
* REL-1.0.0

## 12. Related Tasks
* TASK-GEO-LANGUAGE-001-001
* TASK-GEO-LANGUAGE-001-002
* TASK-GEO-LANGUAGE-001-003
* TASK-GEO-LANGUAGE-001-004
* TASK-GEO-LANGUAGE-001-005
* TASK-GEO-LANGUAGE-001-006
* TASK-GEO-LANGUAGE-001-007
* TASK-GEO-LANGUAGE-001-008
%!(EXTRA string=GEO-LANGUAGE-001)

## Related Documentation

### Functional Documentation

- docs/01_functionality_docs/languages-docs.md

### API Documentation

- docs/02_api/endpoints.md
- docs/02_api/drey-Geo.postman_collection.json

## Traceability

This feature is functionally documented in the corresponding Functional Documentation and its API behavior is defined in the API documentation. Any implementation changes affecting this feature must keep these documents synchronized.
