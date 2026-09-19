# Funcionalidad: Perfil y Gestión de Usuarios (Users)

- **ID de Funcionalidad**: FUNC-USR-MANAGEMENT
- **Capa**: Protected / Secured Controllers / Hexagonal Domain
- **Prefijo de Ruta**: `/api/v1/users/protected` (soporta también `/api/v1/users`)
- **Autenticación requerida**: `Bearer <JWT>` (Obligatorio)
- **Autorización requerida**: Permiso `USERS_CREATE`
- **Consumo de datos**: `application/json`
- **Producción de datos**: `application/json`

---

## 1. Descripción y Propósito

Permite a los usuarios y administradores autenticados gestionar usuarios en la plataforma de eventos deportivos (`api_events_sports`), asegurando el principio de menor privilegio mediante autorización basada en permisos (`USERS_CREATE`) y resguardando la confidencialidad de datos sensibles (los hashes de contraseñas y contraseñas jamás son expuestos en ningún DTO de salida ni en logs).

---

## 2. Endpoints Definidos

### A. Crear Usuario (`POST /api/v1/users/protected/users` o `POST /api/v1/users`)

Crea un nuevo usuario en la base de datos validando la unicidad del correo electrónico (`email`) y del nombre de usuario (`username`), cifrando la contraseña mediante BCrypt a través de `PasswordEncoderPort`, y asignando roles según el modelo relacional.

#### Encabezados Requeridos
| Encabezado | Valor |
| :--- | :--- |
| `Authorization` | `Bearer {{auth_token}}` |
| `Content-Type` | `application/json` |
| `Accept` | `application/json` |

#### Permiso Requerido
- `USERS_CREATE`

#### Cuerpo de la Petición (Request Payload)
```json
{
  "username": "carlos_gomez",
  "email": "carlos.gomez@sportsevents.com",
  "password": "Password123+",
  "firstName": "Carlos",
  "lastName": "Gómez",
  "phone": "+573001112233",
  "roles": [
    "OPERATOR"
  ]
}
```

#### Reglas de Validación de Campos
| Campo | Tipo | Requerido | Restricciones |
| :--- | :--- | :--- | :--- |
| `username` | `String` | Sí | `@NotBlank`, min 3, max 100 caracteres, único |
| `email` | `String` | Sí | `@NotBlank`, formato email válido, único |
| `password` | `String` | Sí | `@NotBlank`, min 6, max 100 caracteres |
| `firstName` | `String` | Sí | `@NotBlank`, max 100 caracteres |
| `lastName` | `String` | Sí | `@NotBlank`, max 150 caracteres |
| `phone` | `String` | No | Opcional, max 50 caracteres |
| `roles` | `Set<String>` | No | Opcional. Si no se envía, por defecto se asigna `OPERATOR` |

#### Respuesta Exitosa (HTTP 201 Created)
```json
{
  "success": true,
  "status": "success",
  "message": "User created successfully",
  "data": {
    "id": 2,
    "username": "carlos_gomez",
    "email": "carlos.gomez@sportsevents.com",
    "firstName": "Carlos",
    "lastName": "Gómez",
    "phone": "+573001112233",
    "status": 1,
    "roles": [
      "OPERATOR"
    ],
    "createdAt": "2026-09-19T14:40:00Z",
    "updatedAt": "2026-09-19T14:40:00Z"
  },
  "timestamp": "2026-09-19T14:40:00Z",
  "requestId": "4c5d6e7f-8a90-1b2c-3d4e-5f6a7b8c9d0e"
}
```

---

### B. Consultar Perfil del Usuario Autenticado (`GET /api/v1/users/me` o `/api/v1/users/{id}`)

#### Encabezados Requeridos
| Encabezado | Valor |
| :--- | :--- |
| `Authorization` | `Bearer {{auth_token}}` |
| `Accept` | `application/json` |

#### Respuesta Exitosa (200 OK)
```json
{
  "success": true,
  "status": "success",
  "message": "User profile retrieved successfully",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@sportsevents.com",
    "firstName": "Administrator",
    "lastName": "System",
    "status": 1,
    "roles": [
      "ADMIN"
    ],
    "lastLoginAt": "2026-09-12T10:17:44Z",
    "createdAt": "2026-09-12T09:56:15Z"
  },
  "timestamp": "2026-09-12T10:25:00Z",
  "requestId": "c5d6e7f8-a90b-1c2d-3e4f-5a6b7c8d9e0f"
}
```

---

## 3. Garantías de Seguridad y Confidencialidad
- **Sin exposición de hash**: Las entidades de dominio `User` y los DTOs de salida `UserResponse` y `UserDto` excluyen estrictamente cualquier contraseña o hash de contraseñas.
- **Control de Acceso Basado en Permisos (PBAC)**: La creación de usuarios requiere explícitamente el permiso `USERS_CREATE` validado por Spring Security `@PreAuthorize("hasAuthority('USERS_CREATE')")`.
- **Manejo de Errores Unificado**: Errores de validación retornan HTTP 400 (`VALIDATION_ERROR`), falta de token retorna HTTP 401 (`token_invalido`), falta de permisos retorna HTTP 403 (`ACCESS_DENIED`), y duplicados retornan HTTP 409 (`DUPLICATE_RESOURCE`).
