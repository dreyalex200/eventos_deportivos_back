# API Contract: [API Name]

## 1. Metadata
- **API ID**: API-{DOMAIN}-{SEQUENCE}
- **Status**: Draft | Active | Completed
- **Created Date**: YYYY-MM-DD

## 2. Endpoint Definition
- **Method**: [GET/POST/PUT/DELETE]
- **Path**: `/api/v1/...`
- **Content-Type**: `application/json`

## 3. Request Contract
[Describe Headers, Path Variables, Query Params, Body Schema]

## 4. Response Contract
[Describe Status Codes, Body Schema for Success]

## 5. Error Contract
[Describe Status Codes, Body Schema for Errors mapped from Domain Errors]

## 6. Security Matrix
[Describe Authentication & Authorization requirements, Required JWT claims/permissions]

## 7. Business Rules
[Rules specific to this endpoint's execution (e.g., rate limiting, idempotency)]

## 8. BDD Scenarios
**Scenario Outline**: [Endpoint Behavior]
Given [State]
When [API call is made]
Then [Response is X]

## 9. OpenAPI Example
```yaml
# OpenAPI spec snippet
```

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
