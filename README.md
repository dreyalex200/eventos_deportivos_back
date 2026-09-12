# Microservicio api_geo

Microservicio de gestión de infraestructura geográfica global, encargado de administrar países, monedas, idiomas, zonas horarias y subdivisiones administrativas.

## 🚀 Características

- **Arquitectura Hexagonal**: Estructura limpia y desacoplada para facilitar el mantenimiento y testing.
- **API REST**: Endpoints estandarizados para clientes y administración.
- **Seguridad**: Autenticación JWT y validación de roles (ADMIN).
- **Resiliencia**: Middleware de recuperación ante panics y Graceful Shutdown.
- **Observabilidad**: Logging estructurado en formato JSON con Logrus.
- **CORS**: Configurado para permitir integraciones seguras con frontends.

## 🛠️ Tecnologías

- **Lenguaje**: Go 1.22+
- **Base de Datos**: MySQL 9.7 LTS
- **Router**: Gorilla Mux
- **JWT**: golang-jwt/jwt
- **Logging**: Logrus

## 📋 Requisitos Previos

- Go 1.22 o superior.
- MySQL 9.7 LTS instalado y en ejecución.
- Variables de entorno configuradas en un archivo `.env` en la raíz del microservicio.

## ⚙️ Configuración

1. Clonar el repositorio.
2. Copiar el archivo `.env.example` a `.env` y configurar las credenciales de base de datos.
3. Ejecutar las migraciones iniciales ubicadas en `docs/02_diseno/esquemas-base-datos/migraciones/001_init.sql`.

## 🏃 Ejecución

```bash
go run cmd/api/main.go
```

El servidor se iniciará por defecto en el puerto `8002` (configurable vía `.env`).

## 🛣️ API Endpoints & Conventions

The API endpoint structure, naming conventions, and standard operations follow a uniform pattern.
Please refer to the Service Conventions for details:
- [Service Conventions](.ai/specs/services/service-conventions.md)
- [Geo Service Definition](.ai/specs/services/api-geo.md)

## 📄 Documentación

Toda la documentación detallada del proyecto se encuentra en la estructura `.ai/specs/` y el archivo general `doc.md`.
- [Especificaciones Funcionales](.ai/specs/requirements/functional.md)
- [Esquema de Base de Datos](.ai/specs/decisions/database-schema.md)
- [Reglas y Flujos](.ai/rules/)
