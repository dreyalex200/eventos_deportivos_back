# Documentación Funcional de Endpoints y Servicios (api_events_sports)

Este directorio documenta de forma exhaustiva las capacidades funcionales del microservicio `api_events_sports` desarrollado con **Spring Boot 3.4.3**, **Java 21** y **MySQL 9.7** bajo **Arquitectura Hexagonal**.

---

## 📁 Índice de Documentación

### 1. Endpoints Anónimos (`anonymous/`)
Endpoints que no requieren cabecera `Authorization: Bearer <token>` para su invocación:

- [Autenticación de Administrador (Login)](./anonymous/authentication.md): Inicio de sesión con credenciales, validaciones, ciclo de emisión JWT y manejo uniforme de errores.
- [Diagnóstico y Monitoreo (Actuator)](./anonymous/health_and_monitoring.md): Verificación de disponibilidad (`/actuator/health`) e información del sistema (`/actuator/info`).

---

### 2. Endpoints y Seguridad Protegida (`protected/`)
Mecanismos de seguridad, endpoints activos y especificaciones de módulos protegidos:

- [Seguridad y Autenticación JWT](./protected/jwt_authentication_and_security.md): Arquitectura de seguridad stateless, estructura real de claims JJWT, mecanismos de autenticación (JWT Bearer y S2S App Token), autorización basada en roles y permisos (RBAC/PBAC), ciclo de vida del token y política CORS.
- [Gestión de Usuarios](./protected/users_management.md): Operación implementada de creación de usuarios (`POST /api/v1/users/protected/users`), validaciones de entrada, encriptación BCrypt, asignación de roles y catálogo de operaciones en backlog.
- [Gestión de Eventos Deportivos](./protected/events_management.md): Estado de implementación (Backlog), modelo relacional preparado en base de datos MySQL y especificación funcional de endpoints proyectados.

---

## 🧪 Colección de Postman

La colección de Postman lista para importar se encuentra ubicada en:
[`docs/02_api/EventSports.postman_collection.json`](../02_api/EventSports.postman_collection.json)

Incluye:
- Variables preconfiguradas (`baseUrl`, `admin_email`, `admin_password`).
- Pruebas automatizadas (test scripts) que capturan el token JWT y lo asignan automáticamente a `auth_token`.
- Muestras de respuestas exitosas (200, 201) y de error (400, 401, 403, 409).
