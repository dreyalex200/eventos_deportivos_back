# API Endpoints: Microservicio api_events_sports

Este documento detalla los endpoints disponibles en la plataforma de gestión de eventos deportivos (`api_events_sports`).

## 🔐 Autenticación y Autorización

El microservicio utiliza autenticación basada en JSON Web Tokens (JWT Bearer):
- Header: `Authorization: Bearer <jwt>`
- Algoritmo de firma: HMAC-SHA256 (`HS256`).
- Autorización: Basada en roles (`ADMIN`, `OPERATOR`, `AUDITOR`) y permisos granulares (`USERS_CREATE`, `USERS_READ`, etc.).

---

## 📡 Módulo 1: Autenticación Anónima (Pública)

| Método | Endpoint | Descripción | Permiso Requerido | Autenticación |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/anonymous/login` | Autenticación de usuario con email y contraseña, retorno de JWT | Ninguno | Ninguna (Pública) |
| `POST` | `/api/v1/auth/login` | Alias de login para retrocompatibilidad | Ninguno | Ninguna (Pública) |

---

## 👤 Módulo 2: Gestión de Usuarios (Protegida)

| Método | Endpoint | Descripción | Permiso Requerido | Autenticación |
|---|---|---|---|---|
| `POST` | `/api/v1/users/protected/users` | Creación de nuevo usuario en el sistema con roles | `USERS_CREATE` | Bearer JWT |
| `POST` | `/api/v1/users` | Alias de creación de usuario | `USERS_CREATE` | Bearer JWT |
| `GET` | `/api/v1/users/me` | Consulta del perfil del usuario autenticado | Ninguno (Usuario autenticado) | Bearer JWT |

---

## 🏆 Módulo 3: Gestión de Eventos Deportivos (Protegida)

| Método | Endpoint | Descripción | Rol / Permiso Requerido | Autenticación |
|---|---|---|---|---|
| `GET` | `/api/v1/events` | Listado paginado de eventos deportivos | Autenticado | Bearer JWT |
| `GET` | `/api/v1/events/{id}` | Detalle de un evento deportivo | Autenticado | Bearer JWT |
| `POST` | `/api/v1/events` | Creación de nuevo evento deportivo | `ADMIN` | Bearer JWT |
| `PUT` | `/api/v1/events/{id}` | Actualización de evento deportivo | `ADMIN` | Bearer JWT |
| `DELETE` | `/api/v1/events/{id}` | Eliminación de evento deportivo | `ADMIN` | Bearer JWT |

---

## 🛠️ Módulo 4: Monitoreo y Diagnóstico (Actuator)

| Método | Endpoint | Descripción | Autenticación |
|---|---|---|---|
| `GET` | `/actuator/health` | Estado de salud y disponibilidad del servicio | Pública |
| `GET` | `/actuator/info` | Información general de la aplicación | Pública |
| `GET` | `/actuator/prometheus` | Métricas en formato Prometheus | Pública |

---

## 🛠️ Códigos de Respuesta Estándar

- **200 OK**: Solicitud exitosa (GET, PUT, DELETE, POST login).
- **201 Created**: Recurso creado exitosamente (POST usuarios, eventos).
- **400 Bad Request**: Parámetros inválidos o error de validación (`VALIDATION_ERROR`).
- **401 Unauthorized**: Falta de token, token inválido o expirado (`INVALID_CREDENTIALS`, `token_invalido`, `USER_INACTIVE`).
- **403 Forbidden**: Token válido pero permisos insuficientes (`ACCESS_DENIED`, `FORBIDDEN_ROLE`).
- **404 Not Found**: Ruta inexistente (`ROUTE_NOT_FOUND`).
- **409 Conflict**: Conflicto por recurso duplicado (`DUPLICATE_RESOURCE`).
- **500 Internal Server Error**: Error interno del servidor (`INTERNAL_SERVER_ERROR`).
