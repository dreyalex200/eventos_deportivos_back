# ❌ Códigos de Error - api_events_sports

Este documento describe los códigos de error unificados devueltos por el microservicio `api_events_sports`.

## 🚦 Errores HTTP y Códigos de Dominio

| Código HTTP | Código Interno | Descripción |
|---|---|---|
| **400** | `VALIDATION_ERROR` | Error en validación de restricciones `@Valid` en el cuerpo o parámetros de la petición. |
| **401** | `INVALID_CREDENTIALS` | Credenciales de inicio de sesión erróneas (correo o contraseña no coinciden). |
| **401** | `USER_INACTIVE` | La cuenta de usuario se encuentra deshabilitada o inactiva (`status != 1`). |
| **401** | `token_invalido` | Token JWT ausente, expirado o con firma digital inválida. |
| **403** | `ACCESS_DENIED` | Usuario autenticado pero carece del permiso granular requerido (ej: `USERS_CREATE`). |
| **403** | `FORBIDDEN_ROLE` | Usuario autenticado pero carece del rol requerido en el sistema (ej: `ADMIN`). |
| **404** | `ROUTE_NOT_FOUND` | Ruta o endpoint HTTP no registrado en el DispatcherServlet de Spring. |
| **409** | `DUPLICATE_RESOURCE` | Intento de registrar un recurso único ya existente (ej: `email` o `username` en uso). |
| **500** | `INTERNAL_SERVER_ERROR` | Error inesperado no controlado en el servidor o infraestructura. |

---

## 🔒 Reglas de Seguridad en Errores
- Los errores de autenticación (`INVALID_CREDENTIALS`) nunca indican específicamente si el correo electrónico existe o no, previniendo ataques de enumeración de usuarios.
- Ningún error o traza expone contraseñas en texto plano ni hashes BCrypt.
