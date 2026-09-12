# Feature: Timezones

## 1. Metadata
- **Requirement ID**: REQ-GEO-TIMEZONE-001
- **Feature ID**: FEATURE-GEO-TIMEZONE-001
- **Domain**: Geo
- **Module**: Timezones
- **Version**: 1.0.0
- **Status**: Active
- **Created Date**: 2026-06-15
- **Author**: Antigravity SDD Agent

## 2. Business Objective
Provide a centralized catalog of timezones and UTC offsets.

## 3. Domain Entities
**Timezone**
| Field | Type |
|-------|------|
| ID | UUID |
| IsActive | Boolean |
| CreatedAt | Timestamp |
| UpdatedAt | Timestamp |
| DeletedAt | Timestamp |
*Additional Fields*: Name (String), UtcOffset (String)

## 4. Permissions
- **Read**: `API_GEO_TIMEZONES_READ`
- **Write**: `API_GEO_TIMEZONES_WRITE`

## 5. Business Rules
- **BR-001**: Timezone name must be unique.


## 6. Dependencies
- **Inbound**: Admin API HTTP calls
- **Outbound**: MySQL 9.7 LTS persistence

## 7. Supported Operations
### Administration
- List Timezones
- Get Timezone By ID
- Create Timezone
- Update Timezone
- Change Status
- Soft Delete
- Hard Delete
- Restore

## 8. BDD Scenarios

### User Story
```gherkin
Feature: Timezones Management

As an administrator
I want to manage Timezones
So that provide a centralized catalog of timezones and utc offsets.
```

### Scenarios
```gherkin
Scenario: Create Timezone Successfully
Given an administrator with permission API_GEO_TIMEZONES_WRITE
And a valid payload
When the administrator creates a Timezone
Then the Timezone is persisted
And HTTP 201 is returned
```

## 9. Acceptance Criteria
- [ ] AC-001: Creation succeeds with valid data (Traceable to SCN-GEO-TIMEZONE-001)
- [ ] AC-002: Validation rejects invalid payloads

## 10. Traceability Matrix
| Requirement | Feature | Scenario | Task | Use Case | Repository | API | Test | Release |
|-------------|---------|----------|------|----------|------------|-----|------|---------|
| REQ-GEO-TIMEZONE-001 | FEATURE-GEO-TIMEZONE-001 | SCN-GEO-TIMEZONE-001 | TASK-GEO-TIMEZONE-001-001 | UC-GEO-TIMEZONE-001 | REPO-GEO-TIMEZONE-001 | API-GEO-TIMEZONE-001 | TEST-GEO-TIMEZONE-001 | REL-1.0.0 |

## 11. Related Artifacts
### Requirements
* REQ-GEO-TIMEZONE-001

### Scenarios
* SCN-GEO-TIMEZONE-001

### Use Cases
* UC-GEO-TIMEZONE-001

### Repositories
* REPO-GEO-TIMEZONE-001

### APIs
* API-GEO-TIMEZONE-001

### Tests
* TEST-GEO-TIMEZONE-001

### Releases
* REL-1.0.0

## 12. Related Tasks
* TASK-GEO-TIMEZONE-001-001
* TASK-GEO-TIMEZONE-001-002
* TASK-GEO-TIMEZONE-001-003
* TASK-GEO-TIMEZONE-001-004
* TASK-GEO-TIMEZONE-001-005
* TASK-GEO-TIMEZONE-001-006
* TASK-GEO-TIMEZONE-001-007
* TASK-GEO-TIMEZONE-001-008
%!(EXTRA string=GEO-TIMEZONE-001)

## Related Documentation

### Functional Documentation

- docs/01_functionality_docs/timezones-docs.md

### API Documentation

- docs/02_api/endpoints.md
- docs/02_api/drey-Geo.postman_collection.json

## Traceability

This feature is functionally documented in the corresponding Functional Documentation and its API behavior is defined in the API documentation. Any implementation changes affecting this feature must keep these documents synchronized.
