# Feature: Currencies

## 1. Metadata
- **Requirement ID**: REQ-GEO-CURRENCY-001
- **Feature ID**: FEATURE-GEO-CURRENCY-001
- **Domain**: Geo
- **Module**: Currencies
- **Version**: 1.0.0
- **Status**: Active
- **Created Date**: 2026-06-15
- **Author**: Antigravity SDD Agent

## 2. Business Objective
Provide a centralized catalog of currencies for financial operations.

## 3. Domain Entities
**Currency**
| Field | Type |
|-------|------|
| ID | UUID |
| IsActive | Boolean |
| CreatedAt | Timestamp |
| UpdatedAt | Timestamp |
| DeletedAt | Timestamp |
*Additional Fields*: Code (String), NumericCode (String), Name (String), Symbol (String), IsPrimary (Boolean)

## 4. Permissions
- **Read**: `API_GEO_CURRENCIES_READ`
- **Write**: `API_GEO_CURRENCIES_WRITE`

## 5. Business Rules
- **BR-001**: ISO Currency Code must be exactly 3 characters.
- **BR-002**: Code must be unique.


## 6. Dependencies
- **Inbound**: Admin API HTTP calls
- **Outbound**: MySQL 9.7 LTS persistence

## 7. Supported Operations
### Administration
- List Currencies
- Get Currency By ID
- Create Currency
- Update Currency
- Change Status
- Soft Delete
- Hard Delete
- Restore

## 8. BDD Scenarios

### User Story
```gherkin
Feature: Currencies Management

As an administrator
I want to manage Currencies
So that provide a centralized catalog of currencies for financial operations.
```

### Scenarios
```gherkin
Scenario: Create Currency Successfully
Given an administrator with permission API_GEO_CURRENCIES_WRITE
And a valid payload
When the administrator creates a Currency
Then the Currency is persisted
And HTTP 201 is returned
```

## 9. Acceptance Criteria
- [ ] AC-001: Creation succeeds with valid data (Traceable to SCN-GEO-CURRENCY-001)
- [ ] AC-002: Validation rejects invalid payloads

## 10. Traceability Matrix
| Requirement | Feature | Scenario | Task | Use Case | Repository | API | Test | Release |
|-------------|---------|----------|------|----------|------------|-----|------|---------|
| REQ-GEO-CURRENCY-001 | FEATURE-GEO-CURRENCY-001 | SCN-GEO-CURRENCY-001 | TASK-GEO-CURRENCY-001-001 | UC-GEO-CURRENCY-001 | REPO-GEO-CURRENCY-001 | API-GEO-CURRENCY-001 | TEST-GEO-CURRENCY-001 | REL-1.0.0 |

## 11. Related Artifacts
### Requirements
* REQ-GEO-CURRENCY-001

### Scenarios
* SCN-GEO-CURRENCY-001

### Use Cases
* UC-GEO-CURRENCY-001

### Repositories
* REPO-GEO-CURRENCY-001

### APIs
* API-GEO-CURRENCY-001

### Tests
* TEST-GEO-CURRENCY-001

### Releases
* REL-1.0.0

## 12. Related Tasks
* TASK-GEO-CURRENCY-001-001
* TASK-GEO-CURRENCY-001-002
* TASK-GEO-CURRENCY-001-003
* TASK-GEO-CURRENCY-001-004
* TASK-GEO-CURRENCY-001-005
* TASK-GEO-CURRENCY-001-006
* TASK-GEO-CURRENCY-001-007
* TASK-GEO-CURRENCY-001-008
%!(EXTRA string=GEO-CURRENCY-001)

## Related Documentation

### Functional Documentation

- docs/01_functionality_docs/currencies-docs.md

### API Documentation

- docs/02_api/endpoints.md
- docs/02_api/drey-Geo.postman_collection.json

## Traceability

This feature is functionally documented in the corresponding Functional Documentation and its API behavior is defined in the API documentation. Any implementation changes affecting this feature must keep these documents synchronized.
