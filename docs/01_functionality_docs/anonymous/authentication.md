# Funcionalidad: Autenticación de Administrador (Login)

- **ID de Funcionalidad**: FUNC-AUTH-LOGIN
- **Capa**: Anonymous / Inbound Controller / Hexagonal Application
- **Método**: `POST`
- **Ruta**: `/api/v1/auth/login`
- **Autenticación requerida**: Ninguna (Pública / Anónima)
- **Consumo de datos**: `application/json`
- **Producción de datos**: `application/json`

---

## 1. Descripción y Propósito

Provee el punto de entrada para que los administradores y usuarios del sistema se autentiquen en el microservicio deportivo (`api_events_sports`). Al enviar credenciales válidas (correo electrónico y contraseña en texto plano), el servicio valida la identidad del usuario, verifica que la cuenta se encuentre activa, comprueba que cuente con privilegios de administrador (`ADMIN`), actualiza la marca temporal del último acceso (`last_login_at`) y emite un token JWT con firma HMAC-SHA256 para autorizar solicitudes subsecuentes en los endpoints protegidos.

---

## 2. Credenciales Iniciales de Semilla (Seed)

Para el entorno de desarrollo y pruebas locales, el microservicio garantiza la existencia idempotente de la cuenta administrativa inicial:

| Campo | Valor |
| :--- | :--- |
| **Email** | `admin@sportsevents.com` |
| **Password** | `Prueba123+` |
| **Rol** | `ADMIN` |
| **Estado** | `active` |
| **Algoritmo de Hash** | BCrypt (Fuerza 10) |

> **Nota**: La inicialización se ejecuta automáticamente al arrancar la aplicación a través de `AdminUserInitializer` y se encuentra respaldada por el script SQL `scripts/db/migrations/mysql/001_seed_admin.sql`.

---

## 3. Arquitectura Hexagonal y Flujo de Ejecución

La implementación sigue de manera estricta los principios de Arquitectura Hexagonal (Puertos y Adaptadores):

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente (Frontend / Postman)
    participant C as AuthController (Infraestructura)
    participant P_In as LoginUseCase (Puerto Entrada)
    participant UC as LoginUseCaseImpl (Aplicación)
    participant P_User as UserRepository (Puerto Salida)
    participant P_Enc as PasswordEncoderPort (Puerto Salida)
    participant P_Tok as TokenProviderPort (Puerto Salida)
    participant DB as Base de Datos (MySQL)

    Cliente->>C: POST /api/v1/auth/login { email, password }
    C->>C: Validar @Valid en LoginRequest
    C->>P_In: execute(LoginCommand)
    P_In->>UC: execute(LoginCommand)
    UC->>P_User: findByEmail(email)
    P_User->>DB: SELECT * FROM users WHERE email = ?
    DB-->>P_User: UserJpaEntity
    P_User-->>UC: Optional<User>

    alt Usuario no encontrado
        UC-->>C: throw InvalidCredentialsException
        C-->>Cliente: 401 Unauthorized (INVALID_CREDENTIALS)
    end

    UC->>UC: user.isActive()
    alt Usuario inactivo
        UC-->>C: throw UserInactiveException
        C-->>Cliente: 401 Unauthorized (USER_INACTIVE)
    end

    UC->>P_Enc: matches(rawPassword, passwordHash)
    alt Contraseña incorrecta
        UC-->>C: throw InvalidCredentialsException
        C-->>Cliente: 401 Unauthorized (INVALID_CREDENTIALS)
    end

    UC->>UC: user.hasRole("ADMIN")
    alt Sin rol de administrador
        UC-->>C: throw UnauthorizedRoleException
        C-->>Cliente: 403 Forbidden (FORBIDDEN_ROLE)
    end

    UC->>P_Tok: generateToken(user)
    P_Tok-->>UC: JWT String
    UC->>P_User: save(user con lastLoginAt actualizado)
    P_User->>DB: UPDATE users SET last_login_at = ?
    UC-->>C: LoginResult (Token, Expiration, UserDto)
    C-->>Cliente: 200 OK (ApiResponse Envelope)
```

---

## 4. Contrato de Entrada (Request Contract)

### Encabezados (Headers)
| Encabezado | Valor Requerido | Descripción |
| :--- | :--- | :--- |
| `Content-Type` | `application/json` | Formato del cuerpo de la petición |
| `Accept` | `application/json` | Formato esperado de respuesta |

### Cuerpo de la Petición (Payload)
Objeto JSON mapeado en el registro Java `LoginRequest`:

```json
{
  "email": "admin@sportsevents.com",
  "password": "Prueba123+"
}
```

### Reglas de Validación de Campos
| Campo | Tipo | Restricciones | Mensaje de Error |
| :--- | :--- | :--- | :--- |
| `email` | `String` | `@NotBlank`, `@Email` | "Email is required" / "Email must be a valid email address" |
| `password` | `String` | `@NotBlank` | "Password is required" |

---

## 5. Contrato de Salida (Response Contract)

El microservicio utiliza el **Unified Envelope Pattern** (`ApiResponse<T>`) del ecosistema para todas las respuestas.

### Respuesta Exitosa (HTTP 200 OK)

```json
{
  "success": true,
  "status": "success",
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IkFETUlOIiwic2NvcGVfaWQiOiI4ZTkzYzdlNS04YTcxLTQyZTYtYmM4MS0xMmI2NTgzYmFmMTUiLCJyb2xlcyI6WyJBRE1JTiJdLCJlbWFpbCI6ImFkbWluQHNwb3J0c2V2ZW50cy5jb20iLCJ1c2VybmFtZSI6ImFkbWluIiwiaWF0IjoxNzg5MjI2MjY0LCJleHAiOjE3ODkzMTI2NjR9.p3sKIQGOTfj82FHsU_6bNXHXXvP_ltekapO5NAkYy_s",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "admin@sportsevents.com",
      "roles": [
        "ADMIN"
      ]
    }
  },
  "timestamp": "2026-09-12T10:17:44Z",
  "requestId": "195eabcd-17c4-4e39-a53c-bd42cea4dac2"
}
```

### Atributos de Respuesta
- `accessToken`: Token JWT firmado mediante HS256.
- `tokenType`: Esquema de autenticación HTTP estándar (`Bearer`).
- `expiresIn`: Tiempo de vida del token en segundos (86400 = 24 horas).
- `user.id`: Identificador numérico único del usuario.
- `user.email`: Correo electrónico del usuario autenticado.
- `user.roles`: Arreglo con los códigos de rol asociados (por ejemplo, `["ADMIN"]`).
- `requestId`: UUID único generado por petición para fines de observabilidad y trazabilidad de logs.

---

## 6. Catálogo de Errores

En caso de fallo, la respuesta HTTP se apega al envelope de error uniforme:

### A. 400 Bad Request — Error de Validación
Ocurre cuando el email está en blanco, tiene un formato inválido o la contraseña falta:
```json
{
  "status": "error",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": "Email must be a valid email address"
  },
  "timestamp": "2026-09-12T10:17:45Z",
  "requestId": "7f5dae81-20d9-45a1-850f-cd6f5c18d7d9"
}
```

### B. 401 Unauthorized — Credenciales Inválidas
Ocurre cuando el email no existe en la base de datos o la contraseña no coincide con el hash BCrypt. Por seguridad, no se revela si el fallo fue por el email o la clave:
```json
{
  "status": "error",
  "error": {
    "code": "INVALID_CREDENTIALS",
    "details": "Invalid email or password"
  },
  "timestamp": "2026-09-12T10:17:45Z",
  "requestId": "b27dc548-e7f9-4d2b-acab-dc7f55e8477d"
}
```

### C. 401 Unauthorized — Usuario Inactivo
Ocurre cuando las credenciales son correctas pero el registro del usuario posee `status != "active"`:
```json
{
  "status": "error",
  "error": {
    "code": "USER_INACTIVE",
    "details": "User account is inactive"
  },
  "timestamp": "2026-09-12T10:17:45Z",
  "requestId": "c3e4f5a6-7b8c-9d0e-1f2a-3b4c5d6e7f8a"
}
```

### D. 403 Forbidden — Privilegios Insuficientes
Ocurre cuando el usuario es válido y activo, pero no cuenta con el rol requerido `ADMIN`:
```json
{
  "status": "error",
  "error": {
    "code": "FORBIDDEN_ROLE",
    "details": "User does not have required administrator privileges"
  },
  "timestamp": "2026-09-12T10:17:45Z",
  "requestId": "d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a"
}
```

---

## 7. Ejemplos de Invocación

### cURL
```bash
curl -i -X POST http://localhost:9000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@sportsevents.com",
    "password": "Prueba123+"
  }'
```

### PowerShell
```powershell
$body = @{
    email = "admin@sportsevents.com"
    password = "Prueba123+"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:9000/api/v1/auth/login" -Method POST -ContentType "application/json" -Body $body
Write-Host "Token: $($response.data.accessToken)"
```
