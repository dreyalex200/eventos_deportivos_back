# Documentación Funcional de Endpoints y Servicios (api_events_sports)

Este directorio documenta de forma exhaustiva las capacidades funcionales del microservicio `api_events_sports` desarrollado con **Spring Boot 3.4.3**, **Java 21** y **MySQL 9.7** bajo **Arquitectura Hexagonal**.

---

## 📁 Índice de Documentación

### 1. Endpoints Anónimos (`anonymous/`)
Endpoints que no requieren cabecera `Authorization: Bearer <token>` para su invocación:

- [Autenticación de Administrador (Login)](./anonymous/authentication.md): Inicio de sesión con credenciales, validaciones, ciclo de emisión JWT y manejo uniforme de errores.
- [Diagnóstico y Monitoreo (Actuator)](./anonymous/health_and_monitoring.md): Verificación de disponibilidad (`/actuator/health`) e información del sistema (`/actuator/info`).

---

### 2. Endpoints Protegidos (`protected/`)
Endpoints asegurados mediante token JWT Bearer en el encabezado HTTP:

- [Seguridad y Autenticación JWT](./protected/jwt_authentication_and_security.md): Arquitectura de seguridad, estructura de claims, ciclo de vida del token, filtros de seguridad y política CORS.
- [Gestión de Eventos Deportivos](./protected/events_management.md): Catálogo y operaciones CRUD para eventos deportivos.
- [Perfil y Gestión de Usuarios](./protected/users_management.md): Consulta de perfiles y resguardo de datos sensibles.

---

## 🧪 Colección de Postman

La colección completa de Postman lista para importar se encuentra ubicada en:
[`docs/02_api/EventSports.postman_collection.json`](../02_api/EventSports.postman_collection.json)

Incluye:
- Variables preconfiguradas (`baseUrl`, `admin_email`, `admin_password`).
- Pruebas automatizadas (test scripts) que capturan el token JWT y lo asignan automáticamente a `auth_token`.
- Muestras de respuestas exitosas (200, 201) y de error (400, 401, 403).
