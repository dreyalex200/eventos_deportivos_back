# Funcionalidad: Seguridad y Autenticación JWT

- **ID de Funcionalidad**: FUNC-SEC-JWT
- **Capa**: Protected / Security / Filter Chain
- **Alcance**: Todos los endpoints bajo `/api/v1/**` (excepto `/api/v1/auth/**`)
- **Mecanismo**: HTTP Bearer Authentication (JSON Web Tokens - RFC 7519)

---

## 1. Descripción y Arquitectura de Seguridad

El microservicio utiliza una arquitectura de seguridad **stateless** (sin estado de sesión en servidor) implementada sobre Spring Security 6.4.3 y la librería JJWT 0.12.6. Ninguna sesión HTTP (`JSESSIONID`) se almacena en el backend, permitiendo escalabilidad horizontal inmediata.

```mermaid
graph TD
    Client[Cliente / Single Page App] -->|1. Petición con Authorization Header| Filter[JwtAuthenticationFilter]
    Filter -->|2. Extraer Bearer token| Parser[JwtUtils]
    Parser -->|3. Validar firma HS256 & Expiración| Parser
    Parser -->|4. Claims válidos| Context[SecurityContextHolder]
    Parser -->|Token inválido o expirado| EntryPoint[CustomAuthenticationEntryPoint]
    EntryPoint -->|Retornar 401 JSON| Client
    Context -->|5. Continuar a Controlador| Dispatcher[DispatcherServlet / Controller]
```

---

## 2. Estructura y Claims del Token JWT

El token emitido por el endpoint de login utiliza el algoritmo `HS256` (HMAC con SHA-256) firmado mediante una clave secreta configurada en `app.security.jwt.secret`.

### Anatomía de los Claims (Payload)

```json
{
  "sub": "1",
  "role": "ADMIN",
  "scope_id": "8e93c7e5-8a71-42e6-bc81-12b6583baf15",
  "roles": [
    "ADMIN"
  ],
  "permissions": [
    "USERS_CREATE",
    "USERS_READ"
  ],
  "authorization": {
    "role": "ADMIN",
    "roles": [
      "ADMIN"
    ],
    "permissions": [
      "USERS_CREATE",
      "USERS_READ"
    ]
  },
  "email": "admin@sportsevents.com",
  "username": "admin",
  "iat": 1789226264,
  "exp": 1789312664
}
```

### Definición de Claims

| Claim | Tipo | Descripción |
| :--- | :--- | :--- |
| `sub` | `String` | Identificador único del usuario (Subject). Corresponde al ID de la entidad `User`. |
| `email` | `String` | Correo electrónico del usuario autenticado. |
| `username` | `String` | Nombre de usuario o identificador de inicio de sesión. |
| `roles` | `List<String>` | Lista de roles asignados al usuario (ej. `["ADMIN"]`). Mapeados en Spring Security a `ROLE_ADMIN` y `ADMIN`. |
| `permissions` | `List<String>` | Lista de permisos efectivos otorgados al usuario (ej. `["USERS_CREATE", "USERS_READ"]`). Mapeados a authorities en Spring Security. |
| `authorization` | `Object` | Objeto estructurado que contiene los roles y permisos efectivos para interoperabilidad con AuthGuard. |
| `scope_id` | `String` | UUID de ámbito o tenant para segmentación multi-inquilino. |
| `iat` | `NumericDate` | Marca de tiempo de emisión del token (Issued At). |
| `exp` | `NumericDate` | Marca de tiempo de expiración (Expiration Time). |

---


## 3. Ciclo de Vida del Token

- **Tiempo de Validez**: `86400` segundos (24 horas).
- **Configuración (`application.yml`)**:
  ```yaml
  app:
    security:
      jwt:
        secret: ${JWT_SECRET:9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b}
        expiration: ${JWT_EXPIRATION_MS:86400000} # 24 horas en milisegundos
  ```

---

## 4. Uso en Solicitudes Protegidas

Para acceder a cualquier endpoint protegido bajo `/api/v1/**`, los clientes deben enviar el token en el encabezado HTTP estándar `Authorization`:

```http
GET /api/v1/events HTTP/1.1
Host: localhost:9000
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Accept: application/json
```

---

## 5. Respuestas de Error de Seguridad

Si el token no es provisto, está corrupto o ha expirado, la petición es interceptada por `CustomAuthenticationEntryPoint` antes de alcanzar los controladores, retornando el formato de error unificado:

### HTTP 401 Unauthorized (Token no provisto o inválido)
```json
{
  "status": "error",
  "error": {
    "code": "token_invalido",
    "details": "Full authentication is required to access this resource"
  },
  "timestamp": "2026-09-12T10:11:00Z",
  "requestId": "2fa99008-8e6c-486a-8b89-a35208b078a6"
}
```

### HTTP 403 Forbidden (Rol insuficiente)
Si el usuario posee un token válido pero carece del rol exigido por la anotación `@PreAuthorize("hasRole('ADMIN')")`:
```json
{
  "status": "error",
  "error": {
    "code": "FORBIDDEN_ROLE",
    "details": "Access Denied"
  },
  "timestamp": "2026-09-12T10:11:00Z",
  "requestId": "6b7c8d9e-0f1a-2b3c-4d5e-6f7a8b9c0d1e"
}
```

---

## 6. Configuración CORS (Cross-Origin Resource Sharing)

Para permitir que clientes web frontend (por ejemplo, aplicaciones Angular en `http://localhost:4200` o React en `http://localhost:3000`) se comuniquen con la API, el microservicio implementa una configuración CORS explícita en `SecurityConfig`:

- **Orígenes Permitidos**: Patrón universal configurable (`*`) con soporte de credenciales.
- **Métodos Permitidos**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`.
- **Encabezados Permitidos**: Todos (`*`).
- **Encabezados Expuestos**: `Authorization`.
- **Preflight Handling**: Las solicitudes `OPTIONS` son interceptadas y respondidas de inmediato con HTTP 200 OK.
