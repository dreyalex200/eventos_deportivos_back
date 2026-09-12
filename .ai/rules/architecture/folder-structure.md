# Folder Structure — Java Backend Services
**Related**: [clean-architecture.md](./clean-architecture.md) | [dependency-rules.md](./dependency-rules.md) | [hexagonal.md](./hexagonal.md)

---

## Purpose

Define the mandatory folder structure for all Java backend services following Hexagonal Architecture.

---

## Root Structure

```
com.drey.{service_name}/
│
├── Application.java                 # Punto de entrada de Spring Boot
│
├── domain/                          # Capa de Dominio (Corazón del negocio)
│   ├── model/                       # Entidades y Value Objects
│   │   ├── entities/                # User, Role, OtpCode, Token, etc.
│   │   ├── valueobjects/            # Email, Password, DeviceInfo, etc.
│   │   ├── enums/                   # AuthStatus, Purpose, NotificationType, etc.
│   │   └── exceptions/              # Domain exceptions (InvalidCredentials, etc.)
│   ├── ports/                       # Interfaces (Contratos)
│   │   ├── inbound/                 # LoginUseCase, ValidateOtpUseCase
│   │   └── outbound/                # UserRepository, OtpRepository, MessagingPort
│   └── service/                     # Implementaciones de servicios de dominio
│
├── application/                     # Capa de Aplicación (Casos de uso)
│   ├── usecases/                    # AuthenticateUser, GenerateOtp, RevokeToken
│   ├── dtos/                        # Objetos de transferencia (Request/Response record)
│   ├── mappers/                     # Convertidores DTO <-> Domain
│   └── validators/                  # Validadores de lógica de aplicación
│
├── infrastructure/                  # Capa de Infraestructura (Adaptadores)
│   ├── config/                      # SecurityConfig, JwtConfig, RedisConfig
│   ├── controllers/                 # REST Controllers (AuthController)
│   ├── persistence/                 # JPA Repositories y Entities
│   ├── external/                    # Clientes REST (OpenFeign/RestTemplate)
│   └── messaging/                   # RabbitMQ Producers/Consumers
│
└── shared/                          # Código compartido y utilidades
    ├── utils/                       # JwtUtils, CryptoUtils, JsonUtils
    ├── constants/                   # SecurityConstants, ErrorCodes
    └── annotations/                 # Custom annotations
```

---

## Layer Rules

### 1. `domain/` (Domain Layer)
- **Zero Spring framework dependencies** (no `@Service`, `@Autowired`, `@Entity`).
- Pure Java records, classes, and interfaces.
- Represents the core business logic.

### 2. `application/` (Application Layer)
- Defines the Use Cases (Inbound Ports implementation).
- Orchestrates Domain Services and Outbound Ports.
- Can use Spring `@Service` for dependency injection.
- Contains `record` types for DTOs.

### 3. `infrastructure/` (Infrastructure Layer)
- Contains Spring REST `@RestController`.
- Contains Spring Data JPA `@Repository` and `@Entity`.
- Contains RabbitMQ adapters and `@Configuration` classes.
- Implements Domain Outbound Ports.

### 4. `shared/` (Shared Layer)
- Contains cross-cutting utilities and constants.
- Must not contain business logic.

---

## Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| **Packages** | lowercase | `domain.model.entities` |
| **Classes** | PascalCase | `UserRepositoryImpl` |
| **Interfaces** | PascalCase | `UserRepository` |
| **DTOs** | PascalCase + `Request`/`Response` | `LoginRequest`, `AuthResponse` |
| **Tests** | `*Test.java` | `LoginUseCaseTest.java` |

---

## Agent Verification Checklist

- [ ] Folder structure matches `domain/`, `application/`, `infrastructure/`, `shared/` exactly.
- [ ] No Spring or JPA annotations (`@Entity`, `@Table`) exist in the `domain/` layer.
- [ ] DTOs are defined as Java `record` types in `application/dtos/`.
- [ ] Controllers are located in `infrastructure/controllers/`.
- [ ] JPA interfaces are in `infrastructure/persistence/`.
