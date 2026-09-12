# Feature: Calling Codes

## 1. Metadata
- **Requirement ID**: REQ-GEO-CALLING-001
- **Feature ID**: FEATURE-GEO-CALLING-001
- **Domain**: Geo
- **Module**: Calling Codes
- **Version**: 1.0.0
- **Status**: Active
- **Created Date**: 2026-06-15
- **Author**: Antigravity SDD Agent

## 2. Business Objective
Provide a centralized catalog of international calling codes mapped to countries.

## 3. Domain Entities
**CountryCallingCode**
| Field | Type |
|-------|------|
| ID | UUID |
| IsActive | Boolean |
| CreatedAt | Timestamp |
| UpdatedAt | Timestamp |
| DeletedAt | Timestamp |
*Additional Fields*: CountryID (UUID), CallingCode (String)

## 4. Permissions
- **Read**: `API_GEO_CALLING_CODES_READ`
- **Write**: `API_GEO_CALLING_CODES_WRITE`

## 5. Business Rules
- **BR-001**: Calling codes must be unique per country.
- **BR-002**: Deleted calling codes cannot be exposed in active lists.


## 6. Dependencies
- **Inbound**: Admin API HTTP calls
- **Outbound**: MySQL 9.7 LTS persistence

## 7. Supported Operations
### Administration
- List Calling Codes
- Get CountryCallingCode By ID
- Create CountryCallingCode
- Update CountryCallingCode
- Change Status
- Soft Delete
- Hard Delete
- Restore

## 8. BDD Scenarios

### User Story
```gherkin
Feature: Calling Codes Management

As an administrator
I want to manage Calling Codes
So that provide a centralized catalog of international calling codes mapped to countries.
```

### Scenarios
```gherkin
Scenario: Create CountryCallingCode Successfully
Given an administrator with permission API_GEO_CALLING_CODES_WRITE
And a valid payload
When the administrator creates a CountryCallingCode
Then the CountryCallingCode is persisted
And HTTP 201 is returned
```

## 9. Acceptance Criteria
- [ ] AC-001: Creation succeeds with valid data (Traceable to SCN-GEO-CALLING-001)
- [ ] AC-002: Validation rejects invalid payloads

## 10. Traceability Matrix
| Requirement | Feature | Scenario | Task | Use Case | Repository | API | Test | Release |
|-------------|---------|----------|------|----------|------------|-----|------|---------|
| REQ-GEO-CALLING-001 | FEATURE-GEO-CALLING-001 | SCN-GEO-CALLING-001 | TASK-GEO-CALLING-001-001 | UC-GEO-CALLING-001 | REPO-GEO-CALLING-001 | API-GEO-CALLING-001 | TEST-GEO-CALLING-001 | REL-1.0.0 |

## 11. Related Artifacts
### Requirements
* REQ-GEO-CALLING-001

### Scenarios
* SCN-GEO-CALLING-001

### Use Cases
* UC-GEO-CALLING-001

### Repositories
* REPO-GEO-CALLING-001

### APIs
* API-GEO-CALLING-001

### Tests
* TEST-GEO-CALLING-001

### Releases
* REL-1.0.0

## 12. Related Tasks
* TASK-GEO-CALLING-001-001
* TASK-GEO-CALLING-001-002
* TASK-GEO-CALLING-001-003
* TASK-GEO-CALLING-001-004
* TASK-GEO-CALLING-001-005
* TASK-GEO-CALLING-001-006
* TASK-GEO-CALLING-001-007
* TASK-GEO-CALLING-001-008
%!(EXTRA string=GEO-CALLING-001)

## Related Documentation

### Functional Documentation

- docs/01_functionality_docs/calling-codes-docs.md

### API Documentation

- docs/02_api/endpoints.md
- docs/02_api/drey-Geo.postman_collection.json

## Traceability

This feature is functionally documented in the corresponding Functional Documentation and its API behavior is defined in the API documentation. Any implementation changes affecting this feature must keep these documents synchronized.
