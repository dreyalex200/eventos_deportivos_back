# Funcionalidad: Gestión de Eventos Deportivos (Events)

- **ID de Funcionalidad**: FUNC-EVT-MANAGEMENT
- **Capa**: Protected / Secured Controllers / Hexagonal Domain
- **Prefijo de Ruta**: `/api/v1/events`
- **Autenticación requerida**: `Bearer <JWT>` (Obligatorio)
- **Consumo de datos**: `application/json`
- **Producción de datos**: `application/json`

---

## 1. Descripción y Alcance

Permite el ciclo de vida completo (CRUD) de eventos deportivos en la plataforma: consulta paginada, visualización de detalles, creación de eventos, actualización y eliminación lógica.

### Matriz de Control de Acceso Basado en Roles (RBAC)

| Operación | Método HTTP | Ruta | Rol Requerido |
| :--- | :--- | :--- | :--- |
| **Listar Eventos** | `GET` | `/api/v1/events` | Autenticado (`USER`, `ADMIN`) |
| **Consultar Evento por ID** | `GET` | `/api/v1/events/{id}` | Autenticado (`USER`, `ADMIN`) |
| **Crear Evento** | `POST` | `/api/v1/events` | `ADMIN` |
| **Actualizar Evento** | `PUT` | `/api/v1/events/{id}` | `ADMIN` |
| **Eliminar Evento** | `DELETE` | `/api/v1/events/{id}` | `ADMIN` |

---

## 2. Definición de Endpoints

### A. Listar Eventos Paginados (`GET /api/v1/events`)

#### Parámetros de Consulta (Query Params)
| Parámetro | Tipo | Por Defecto | Descripción |
| :--- | :--- | :--- | :--- |
| `page` | `Integer` | `0` | Número de página (base 0) |
| `size` | `Integer` | `10` | Cantidad de elementos por página |
| `sort` | `String` | `startDate,desc` | Campo y dirección de ordenamiento |

#### Respuesta Exitosa (200 OK)
```json
{
  "success": true,
  "status": "success",
  "message": "Events retrieved successfully",
  "data": {
    "items": [
      {
        "id": 1,
        "title": "Torneo Apertura de Fútbol 2026",
        "description": "Campeonato aficionado categoría libre",
        "sportType": "FOOTBALL",
        "location": "Estadio Central",
        "startDate": "2026-10-15T09:00:00",
        "endDate": "2026-10-20T18:00:00",
        "status": "PUBLISHED",
        "maxParticipants": 16,
        "currentParticipants": 8
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2026-09-12T10:20:00Z",
  "requestId": "e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b"
}
```

---

### B. Crear Evento Deportivo (`POST /api/v1/events`)

#### Encabezados
```http
Authorization: Bearer {{auth_token}}
Content-Type: application/json
```

#### Payload de Solicitud
```json
{
  "title": "Torneo Apertura de Fútbol 2026",
  "description": "Campeonato aficionado categoría libre",
  "sportType": "FOOTBALL",
  "location": "Estadio Central",
  "startDate": "2026-10-15T09:00:00",
  "endDate": "2026-10-20T18:00:00",
  "maxParticipants": 16
}
```

#### Respuesta Exitosa (201 Created)
```json
{
  "success": true,
  "status": "success",
  "message": "Event created successfully",
  "data": {
    "id": 1,
    "title": "Torneo Apertura de Fútbol 2026",
    "sportType": "FOOTBALL",
    "status": "DRAFT",
    "createdAt": "2026-09-12T10:21:00Z"
  },
  "timestamp": "2026-09-12T10:21:00Z",
  "requestId": "f2a3b4c5-d6e7-8f9a-0b1c-2d3e4f5a6b7c"
}
```

---

### C. Actualizar Evento Deportivo (`PUT /api/v1/events/{id}`)

#### Payload de Solicitud
```json
{
  "title": "Torneo Apertura de Fútbol 2026 - Final",
  "description": "Descripción actualizada",
  "sportType": "FOOTBALL",
  "location": "Estadio Central Olímpico",
  "startDate": "2026-10-15T10:00:00",
  "endDate": "2026-10-20T19:00:00",
  "maxParticipants": 20
}
```

#### Respuesta Exitosa (200 OK)
```json
{
  "success": true,
  "status": "success",
  "message": "Event updated successfully",
  "data": {
    "id": 1,
    "title": "Torneo Apertura de Fútbol 2026 - Final",
    "status": "PUBLISHED",
    "updatedAt": "2026-09-12T10:22:00Z"
  },
  "timestamp": "2026-09-12T10:22:00Z",
  "requestId": "a3b4c5d6-e7f8-9a0b-1c2d-3e4f5a6b7c8d"
}
```

---

### D. Eliminar Evento Deportivo (`DELETE /api/v1/events/{id}`)

#### Respuesta Exitosa (200 OK)
```json
{
  "success": true,
  "status": "success",
  "message": "Event deleted successfully",
  "data": null,
  "timestamp": "2026-09-12T10:23:00Z",
  "requestId": "b4c5d6e7-f8a9-0b1c-2d3e-4f5a6b7c8d9e"
}
```
