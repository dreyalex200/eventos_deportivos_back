# Hexagonal Architecture (Ports & Adapters) — Java Spring Boot
**Related**: [clean-architecture.md](./clean-architecture.md) | [folder-structure.md](./folder-structure.md)

---

## Purpose

Map the Ports & Adapters pattern to Spring Boot microservices to keep the core business logic completely decoupled from frameworks (Spring), databases (JPA/Hibernate), caches (Redis), and message brokers (RabbitMQ).

---

## Conceptual Model

```
                    ┌──────────────────────────────────────┐
  PRIMARY           │           CORE DOMAIN                │         SECONDARY
  ADAPTERS          │  (Pure Java, zero frameworks)        │         ADAPTERS
  (Driving)         │                                      │         (Driven)
                    │  ┌────────────────────────────────┐  │
  REST Controllers ─┤─▶│   Inbound Ports                │  │
  (Spring MVC)      │  │   (Use Cases)                  │  │
                    │  │                                │  │
  Message Listeners─┤─▶│                                │  │
  (RabbitMQ)        │  └────────────────────────────────┘  │
                    │                                      │
                    │  ┌────────────────────────────────┐  │
                    │  │   Outbound Ports                │  ├──▶ PostgreSQL (JPA)
                    │  │   (Repositories, Clients)       │  ├──▶ Redis (Spring Data)
                    │  └────────────────────────────────┘  ├──▶ External APIs (OpenFeign)
                    │                                      ├──▶ RabbitMQ Producers
                    └──────────────────────────────────────┘
```

---

## Layer Responsibilities

### 1. Domain (`domain/`)
The innermost circle. Pure Java.
- **Models**: `User`, `Role`, `Email` (Value Object).
- **Inbound Ports**: `interface AuthenticateUserUseCase`.
- **Outbound Ports**: `interface UserRepository`.
- **Domain Services**: Business logic that doesn't naturally fit in a single entity.
- **Rule**: NO Spring annotations (`@Service`, `@Entity`, `@Autowired`).

### 2. Application (`application/`)
Orchestration layer.
- **Use Cases Impl**: Classes that implement Inbound Ports and inject Outbound Ports.
- **DTOs**: Java 21 records representing request/response structures.
- **Mappers**: Map between DTOs and Domain Models.
- **Rule**: Can use `@Service` or `@Component` for dependency injection.

### 3. Infrastructure (`infrastructure/`)
The outermost circle. Framework-heavy.
- **Controllers**: `@RestController` mapped to HTTP endpoints.
- **Persistence**: Spring Data JPA `@Repository` and `@Entity`.
- **Messaging**: `@RabbitListener` and `RabbitTemplate`.
- **Rule**: Adapters must implement Outbound Ports.

---

## Example Flow: Login Use Case

### 1. Domain Outbound Port (Interface)
```java
// domain/ports/outbound/UserRepository.java
package com.drey.authguard.domain.ports.outbound;

import com.drey.authguard.domain.model.entities.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
}
```

### 2. Application Inbound Port (UseCase)
```java
// domain/ports/inbound/LoginUseCase.java
package com.drey.authguard.domain.ports.inbound;

import com.drey.authguard.domain.model.entities.Token;

public interface LoginUseCase {
    Token login(String email, String password);
}
```

### 3. Application UseCase Implementation
```java
// application/usecases/LoginUseCaseImpl.java
package com.drey.authguard.application.usecases;

import com.drey.authguard.domain.ports.inbound.LoginUseCase;
import com.drey.authguard.domain.ports.outbound.UserRepository;
import com.drey.authguard.domain.model.entities.Token;
import com.drey.authguard.domain.model.exceptions.InvalidCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCaseImpl implements LoginUseCase {
    
    private final UserRepository userRepository;

    public LoginUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Token login(String email, String password) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException());
        
        if (!user.checkPassword(password)) {
            throw new InvalidCredentialsException();
        }
        // Generate and return token...
        return new Token("jwt-string");
    }
}
```

### 4. Infrastructure Secondary Adapter (Persistence)
```java
// infrastructure/persistence/UserRepositoryAdapter.java
package com.drey.authguard.infrastructure.persistence;

import com.drey.authguard.domain.ports.outbound.UserRepository;
import com.drey.authguard.domain.model.entities.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    
    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
            .map(entity -> new User(entity.getId(), entity.getEmail(), entity.getPassword())); // Map JPA Entity to Domain Model
    }
}
```

### 5. Infrastructure Primary Adapter (Controller)
```java
// infrastructure/controllers/AuthController.java
package com.drey.authguard.infrastructure.controllers;

import com.drey.authguard.domain.ports.inbound.LoginUseCase;
import com.drey.authguard.application.dtos.LoginRequest;
import com.drey.authguard.application.dtos.AuthResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        var token = loginUseCase.login(request.email(), request.password());
        return new AuthResponse(token.getValue(), true);
    }
}
```

---

## Agent Verification Checklist

- [ ] Does the Controller depend on `LoginUseCase` (Inbound Port) instead of `UserRepository` directly?
- [ ] Does `UserRepositoryAdapter` implement `UserRepository` and map JPA Entities to Domain Entities?
- [ ] Is `domain/` completely free of `@Entity`, `@Table`, and `@RestController`?
