# BDD Scenarios: [Feature or Module Name]

## 1. Metadata
- **Scenario ID**: SCN-{DOMAIN}-{SEQUENCE}
- **Status**: Draft | Active | Completed
- **Created Date**: YYYY-MM-DD

> **Note:** Dedicated Scenario documents must be generated ONLY if the feature contains more than 5 scenarios. If 5 or fewer, they remain inline in the Feature document.

## 2. User Story
```gherkin
Feature: [Feature Name]

As a [Role]
I want [Goal]
So that [Benefit]
```

## 3. Scenarios
```gherkin
Scenario: [Scenario Name]
Given [Precondition]
And [Another Precondition]
When [Action]
Then [Expected Result]
And [Another Expected Result]
```

## 4. Data Tables (Scenario Outlines)
```gherkin
Scenario Outline: [Outline Name]
Given [Precondition]
When [Action with <variable>]
Then [Expected Result with <variable>]

Examples:
| variable | expected |
|----------|----------|
| value1   | output1  |
| value2   | output2  |
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
