# Clean Architecture Rules — Java Backend
**Related**: [hexagonal.md](./hexagonal.md) | [dependency-rules.md](./dependency-rules.md)

---

## Purpose

Define the Clean Architecture boundaries for Java Spring Boot microservices.

---

## The Dependency Rule

```
Infrastructure (Controllers, DB, Frameworks)
        ↓
    Application (Use Cases, DTOs)
        ↓
      Domain (Entities, Ports)
```

**Rule**: Dependencies must point INWARD.
- `domain` imports NOTHING from `application` or `infrastructure`.
- `application` imports `domain`, but NOTHING from `infrastructure`.
- `infrastructure` imports `application` and `domain`.

---

## Layer Definitions

### 1. Domain Layer (`domain/`)
- Pure Java logic.
- Entities must encapsulate their own state validation.
- Exceptions must be business-oriented (e.g., `InvalidCredentialsException`, not `SQLException`).

### 2. Application Layer (`application/`)
- Coordinates the workflow.
- Receives Request DTOs, maps them to Domain Entities.
- Calls Domain Services or Ports.
- Maps Domain Entities to Response DTOs.
- DTOs should be Java 21 `record`s.

### 3. Infrastructure Layer (`infrastructure/`)
- Contains all Spring Boot specific code.
- REST Controllers map HTTP requests to Application UseCases.
- Persistence adapters map JPA entities to Domain Entities.

---

## Mapping Between Layers

Entities in the `infrastructure/persistence/` layer are **JPA Entities**. They must be mapped to **Domain Entities** before crossing the boundary into `application` or `domain`.

```java
// infrastructure/persistence/UserJpaEntity.java
@Entity
@Table(name = "users")
public class UserJpaEntity { ... }

// infrastructure/persistence/UserRepositoryImpl.java
@Repository
public class UserRepositoryImpl implements UserRepository {
    // Maps UserJpaEntity to Domain User
}
```

## Agent Verification Checklist

- [ ] Does `domain/` contain any `import org.springframework...` or `import jakarta.persistence...`? If yes, it violates Clean Architecture.
- [ ] Do Application Use Cases depend on interfaces (Ports) rather than concrete implementations?
- [ ] Are Data Transfer Objects (DTOs) used to communicate with Controllers, rather than passing Domain Entities directly to HTTP responses?
