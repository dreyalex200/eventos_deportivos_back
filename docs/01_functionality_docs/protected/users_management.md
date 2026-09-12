# Funcionalidad: Perfil y Gestión de Usuarios (Users)

- **ID de Funcionalidad**: FUNC-USR-PROFILE
- **Capa**: Protected / User Management
- **Prefijo de Ruta**: `/api/v1/users`
- **Autenticación requerida**: `Bearer <JWT>` (Obligatorio)
- **Consumo de datos**: `application/json`
- **Producción de datos**: `application/json`

---

## 1. Descripción y Propósito

Permite a los usuarios autenticados consultar y gestionar información de perfil de usuario en el microservicio deportivo, resguardando la confidencialidad de datos sensibles (como los hashes de contraseñas BCrypt, los cuales jamás son expuestos en ningún DTO de salida).

---

## 2. Endpoints Definidos

### A. Consultar Perfil del Usuario Autenticado (`GET /api/v1/users/me` o `/api/v1/users/{id}`)

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
    "status": "active",
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
- **Sin exposición de hash**: Las entidades de dominio `User` y los DTOs de salida `UserDto` excluyen estrictamente el campo `passwordHash`.
- **Aislamiento por ID / Token**: Los usuarios no administradores solo pueden acceder a su propio perfil determinado por el claim `sub` del JWT verificado.
