# Dependency Rules — Java Backend
**Related**: [clean-architecture.md](./clean-architecture.md) | [hexagonal.md](./hexagonal.md)

---

## Purpose

Define strict boundaries for package imports across the architectural layers in Java Spring Boot. Violations of these rules represent a breakdown of the Hexagonal Architecture.

---

## Layer Import Rules

### 1. Domain Layer (`domain/`)

**Allowed Imports**:
- Standard Java libraries (`java.util.*`, `java.time.*`, `java.math.*`).
- Cross-cutting shared utilities from `shared/` (if strictly necessary and lacking business logic).

**Forbidden Imports**:
- Anything from `org.springframework.*`
- Anything from `jakarta.persistence.*` or `javax.persistence.*` (JPA annotations like `@Entity`, `@Table`)
- Anything from `application/`
- Anything from `infrastructure/`

### 2. Application Layer (`application/`)

**Allowed Imports**:
- Domain classes (`domain.model.*`, `domain.ports.*`).
- Core Spring annotations for dependency injection (`org.springframework.stereotype.Service`, `@Component`).
- Java standard libraries.

**Forbidden Imports**:
- Concrete implementations from `infrastructure/` (e.g., `UserRepositoryAdapter`).
- Spring Web/REST annotations (`@RestController`, `@RequestMapping`).
- Spring Data JPA classes (`@Repository`, `JpaRepository`).

### 3. Infrastructure Layer (`infrastructure/`)

**Allowed Imports**:
- Domain Ports (`domain.ports.inbound.*`, `domain.ports.outbound.*`).
- Application DTOs and Use Cases (`application.usecases.*`, `application.dtos.*`).
- Any required Spring Boot, JPA, or external framework packages.

**Forbidden Imports**:
- Infrastructure packages should generally not import sibling infrastructure packages directly (e.g., Controllers should not import Repositories directly; they should go through Application Use Cases).

---

## Framework Independence Rule

The **Domain Layer** must not know that it is running inside a Spring Boot application or that its data is being saved to PostgreSQL via JPA.

**Violation Example (Domain Entity with JPA):**
```java
// ❌ WRONG: Domain Entity polluted with Infrastructure (JPA) concerns
package com.drey.authguard.domain.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity // <--- VIOLATION
public class User {
    @Id // <--- VIOLATION
    private String id;
}
```

**Correction (Separation):**
```java
// ✅ CORRECT: Pure Java in Domain
package com.drey.authguard.domain.model.entities;

public class User {
    private String id;
}

// ✅ CORRECT: JPA in Infrastructure
package com.drey.authguard.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Table(name = "users")
public class UserJpaEntity {
    @Id
    private String id;
}
```

---

## Agent Verification Checklist

- [ ] Are there any `import org.springframework...` or `import jakarta.persistence...` in the `domain` package? If yes, remove them and fix the architecture.
- [ ] Does any `Controller` import a class from `infrastructure/persistence`? If yes, it must be refactored to call an `application/usecase` instead.
- [ ] Are Domain Entities kept entirely separate from JPA/Database Entities?
