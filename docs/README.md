# Documentación del Proyecto: Microservicio de Eventos Deportivos (api_events_sports)

Bienvenido a la documentación técnica y funcional del microservicio **`api_events_sports`**.

Este microservicio se encarga de la gestión centralizada de eventos deportivos, autenticación de administradores, inscripciones y perfiles dentro del ecosistema Arcadion.

---

## 🎯 Propósito del Microservicio

- **Autenticación y Seguridad:** Inicio de sesión de administradores y usuarios con emisión de tokens JWT (HS256) y validación de roles (`ADMIN`).
- **Gestión de Eventos Deportivos:** Publicación, actualización, consulta y administración del ciclo de vida de eventos deportivos.
- **Observabilidad y Diagnóstico:** Endpoints de salud (`/actuator/health`) y monitoreo de estado para orquestadores y balanceadores.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 (Eclipse Adoptium OpenJDK 21)
- **Framework:** Spring Boot 3.4.3
- **Base de Datos:** MySQL 9.7 (Base de datos: `sports_events`)
- **Persistencia:** Spring Data JPA / Hibernate 6.6.8
- **Seguridad:** Spring Security 6.4.3 + JJWT 0.12.6 (Stateless + JWT)
- **Construcción:** Gradle 8.12.1
- **Arquitectura:** Hexagonal / Clean Architecture (Puertos y Adaptadores)
- **Estándar API:** Envelope Pattern unificado (`ApiResponse<T>`)

---

## 📁 Estructura de la Documentación

- [`01_functionality_docs/`](./01_functionality_docs): Especificaciones funcionales completas divididas en:
  - [`anonymous/`](./01_functionality_docs/anonymous): Endpoints públicos (Login, Health Check).
  - [`protected/`](./01_functionality_docs/protected): Endpoints protegidos por JWT (Eventos, Usuarios, Seguridad).
- [`02_api/`](./02_api): Colección exportada de Postman ([`EventSports.postman_collection.json`](./02_api/EventSports.postman_collection.json)).

---

## 🚀 Inicio Rápido para Desarrolladores

1. **Configuración de Base de Datos**: Asegurarse de contar con una instancia MySQL ejecutándose en el puerto `3306` con la base de datos `sports_events`.
2. **Ejecución del Microservicio**:
   ```bash
   ./gradlew bootRun
   ```
   *O mediante el ejecutable JAR empaquetado:*
   ```bash
   java -jar build/libs/api_events_sports-0.0.1-SNAPSHOT.jar
   ```
3. **Credenciales Iniciales de Administrador**:
   - **Email**: `admin@sportsevents.com`
   - **Password**: `Prueba123+`
4. **Importar Postman**:
   - Importar `docs/02_api/EventSports.postman_collection.json` en Postman.
   - Ejecutar la petición `Anonymous > Auth > Login - Administrator (Success)`.
   - El script de prueba almacenará automáticamente el token en `auth_token`, autorizando las peticiones de la carpeta `Protected`.
