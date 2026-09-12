# Feature: Subdivisions

## 1. Metadata
- **Requirement ID**: REQ-GEO-SUBDIVISION-001
- **Feature ID**: FEATURE-GEO-SUBDIVISION-001
- **Domain**: Geo
- **Module**: Subdivisions
- **Version**: 1.0.0
- **Status**: Active
- **Created Date**: 2026-06-15
- **Author**: Antigravity SDD Agent

## 2. Business Objective
Provide a centralized catalog of states/provinces/subdivisions per country.

## 3. Domain Entities
**Subdivision**
| Field | Type |
|-------|------|
| ID | UUID |
| IsActive | Boolean |
| CreatedAt | Timestamp |
| UpdatedAt | Timestamp |
| DeletedAt | Timestamp |
*Additional Fields*: CountryID (UUID), Code (String), Name (String), Type (String)

## 4. Permissions
- **Read**: `API_GEO_SUBDIVISIONS_READ`
- **Write**: `API_GEO_SUBDIVISIONS_WRITE`

## 5. Business Rules
- **BR-001**: Subdivision code must be unique per country.


## 6. Dependencies
- **Inbound**: Admin API HTTP calls
- **Outbound**: MySQL 9.7 LTS persistence

## 7. Supported Operations
### Administration
- List Subdivisions
- Get Subdivision By ID
- Create Subdivision
- Update Subdivision
- Change Status
- Soft Delete
- Hard Delete
- Restore

## 8. BDD Scenarios

### User Story
```gherkin
Feature: Subdivisions Management

As an administrator
I want to manage Subdivisions
So that provide a centralized catalog of states/provinces/subdivisions per country.
```

### Scenarios
```gherkin
Scenario: Create Subdivision Successfully
Given an administrator with permission API_GEO_SUBDIVISIONS_WRITE
And a valid payload
When the administrator creates a Subdivision
Then the Subdivision is persisted
And HTTP 201 is returned
```

## 9. Acceptance Criteria
- [ ] AC-001: Creation succeeds with valid data (Traceable to SCN-GEO-SUBDIVISION-001)
- [ ] AC-002: Validation rejects invalid payloads

## 10. Traceability Matrix
| Requirement | Feature | Scenario | Task | Use Case | Repository | API | Test | Release |
|-------------|---------|----------|------|----------|------------|-----|------|---------|
| REQ-GEO-SUBDIVISION-001 | FEATURE-GEO-SUBDIVISION-001 | SCN-GEO-SUBDIVISION-001 | TASK-GEO-SUBDIVISION-001-001 | UC-GEO-SUBDIVISION-001 | REPO-GEO-SUBDIVISION-001 | API-GEO-SUBDIVISION-001 | TEST-GEO-SUBDIVISION-001 | REL-1.0.0 |

## 11. Related Artifacts
### Requirements
* REQ-GEO-SUBDIVISION-001

### Scenarios
* SCN-GEO-SUBDIVISION-001

### Use Cases
* UC-GEO-SUBDIVISION-001

### Repositories
* REPO-GEO-SUBDIVISION-001

### APIs
* API-GEO-SUBDIVISION-001

### Tests
* TEST-GEO-SUBDIVISION-001

### Releases
* REL-1.0.0

## 12. Related Tasks
* TASK-GEO-SUBDIVISION-001-001
* TASK-GEO-SUBDIVISION-001-002
* TASK-GEO-SUBDIVISION-001-003
* TASK-GEO-SUBDIVISION-001-004
* TASK-GEO-SUBDIVISION-001-005
* TASK-GEO-SUBDIVISION-001-006
* TASK-GEO-SUBDIVISION-001-007
* TASK-GEO-SUBDIVISION-001-008
%!(EXTRA string=GEO-SUBDIVISION-001)

## Related Documentation

### Functional Documentation

- docs/01_functionality_docs/subdivisions-docs.md

### API Documentation

- docs/02_api/endpoints.md
- docs/02_api/drey-Geo.postman_collection.json

## Traceability

This feature is functionally documented in the corresponding Functional Documentation and its API behavior is defined in the API documentation. Any implementation changes affecting this feature must keep these documents synchronized.
