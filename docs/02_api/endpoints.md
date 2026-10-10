# API Endpoints: Microservicio api_events_sports

Este documento detalla los endpoints implementados y proyectados en la plataforma de gestión de eventos deportivos (`api_events_sports`).

## 🔐 Autenticación y Autorización

El microservicio utiliza dos mecanismos de seguridad en la capa de transporte HTTP:
1. **HTTP Bearer JWT**: `Authorization: Bearer <jwt>`
   - Algoritmo de firma: HMAC-SHA256 (`HS256`).
   - Autorización: Basada en roles (`ADMIN`, `OPERATOR`, `AUDITOR`) y permisos granulares (`USERS_CREATE`, `USERS_READ`, etc.).
2. **Service-to-Service (S2S)**: Encabezados `X-App-Token` y `X-Scope-Id` para comunicación interservicios.

---

## 📡 Módulo 1: Autenticación Anónima (Pública - Implementado)

| Método | Endpoint | Descripción | Permiso Requerido | Autenticación | Estado |
|---|---|---|---|---|---|
| `POST` | `/api/v1/auth/anonymous/login` | Autenticación de usuario administrador con email y contraseña, retorno de JWT | Ninguno | Ninguna (Pública) | ✅ Implementado |
| `POST` | `/api/v1/auth/login` | Alias de inicio de sesión | Ninguno | Ninguna (Pública) | ✅ Implementado |

---

## 👤 Módulo 2: Gestión de Usuarios (Protegida)

| Método | Endpoint | Descripción | Permiso Requerido | Autenticación | Estado |
|---|---|---|---|---|---|
| `POST` | `/api/v1/users/protected/users` | Creación de nuevo usuario con asignación de roles | Authority `USERS_CREATE` | Bearer JWT | ✅ Implementado |
| `POST` | `/api/v1/users/protected` | Alias canónico de creación de usuario | Authority `USERS_CREATE` | Bearer JWT | ✅ Implementado |
| `POST` | `/api/v1/users` | Alias directo de creación de usuario | Authority `USERS_CREATE` | Bearer JWT | ✅ Implementado |
| `GET` | `/api/v1/users/me` | Consulta del perfil del usuario autenticado | Autenticado | Bearer JWT | ❌ Backlog (Pendiente) |
| `GET` | `/api/v1/users/{id}` | Consulta de usuario por ID | Authority `USERS_READ` | Bearer JWT | ❌ Backlog (Pendiente) |

---

## 🏆 Módulo 3: Gestión de Eventos Deportivos (Protegida)

> **Nota**: El modelo de datos relacional para eventos se encuentra definido en `000_init.sql`, pero la capa de controladores y casos de uso en backend está pendiente de codificación.

| Método | Endpoint | Descripción | Rol / Permiso Previsto | Autenticación | Estado |
|---|---|---|---|---|---|
| `GET` | `/api/v1/events` | Listado paginado de eventos deportivos | Autenticado | Bearer JWT | ❌ Backlog (Pendiente) |
| `GET` | `/api/v1/events/{id}` | Detalle de un evento deportivo | Autenticado | Bearer JWT | ❌ Backlog (Pendiente) |
| `POST` | `/api/v1/events` | Creación de nuevo evento deportivo | `ADMIN` / `EVENTS_CREATE` | Bearer JWT | ❌ Backlog (Pendiente) |
| `PUT` | `/api/v1/events/{id}` | Actualización de evento deportivo | `ADMIN` / `EVENTS_UPDATE` | Bearer JWT | ❌ Backlog (Pendiente) |
| `DELETE` | `/api/v1/events/{id}` | Eliminación lógica de evento deportivo | `ADMIN` / `EVENTS_DELETE` | Bearer JWT | ❌ Backlog (Pendiente) |

---

## 🛠️ Módulo 4: Monitoreo y Diagnóstico (Actuator - Implementado)

| Método | Endpoint | Descripción | Autenticación | Estado |
|---|---|---|---|---|
| `GET` | `/actuator/health` | Estado de salud y disponibilidad del servicio | Pública | ✅ Implementado |
| `GET` | `/actuator/info` | Información general de la aplicación | Pública | ✅ Implementado |
| `GET` | `/actuator/prometheus` | Métricas en formato Prometheus | Pública | ✅ Implementado |

---

## 🛠️ Códigos de Respuesta Estándar

- **200 OK**: Solicitud exitosa (POST login, Actuator).
- **201 Created**: Recurso creado exitosamente (POST usuarios).
- **400 Bad Request**: Parámetros inválidos o error de validación (`VALIDATION_ERROR`).
- **401 Unauthorized**: Falta de token, token inválido o expirado (`INVALID_CREDENTIALS`, `token_invalido`, `USER_INACTIVE`).
- **403 Forbidden**: Token válido pero permisos insuficientes (`ACCESS_DENIED`, `FORBIDDEN_ROLE`).
- **404 Not Found**: Ruta inexistente (`ROUTE_NOT_FOUND`).
- **409 Conflict**: Conflicto por recurso duplicado (`DUPLICATE_RESOURCE`).
- **500 Internal Server Error**: Error interno del servidor (`INTERNAL_SERVER_ERROR`).
