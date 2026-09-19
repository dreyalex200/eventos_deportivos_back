# Ejemplos de Request y Response: api_events_sports

Este documento proporciona ejemplos detallados de las peticiones y respuestas para los endpoints principales de autenticación y gestión de usuarios del microservicio `api_events_sports`, siguiendo el Unified Envelope Pattern del ecosistema.

---

## 🔐 Módulo 1: Autenticación Anónima

### 1.1 Inicio de Sesión Exitoso (Login)
**POST** `/api/v1/auth/anonymous/login` (o `/api/v1/auth/login`)

**Headers:**
```http
Content-Type: application/json
Accept: application/json
```

**Request Body:**
```json
{
  "email": "admin@sportsevents.com",
  "password": "Prueba123+"
}
```

**Response Body (200 OK):**
```json
{
  "success": true,
  "status": "success",
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IkFETUlOIiwic2NvcGVfaWQiOiI4ZTkzYzdlNS04YTcxLTQyZTYtYmM4MS0xMmI2NTgzYmFmMTUiLCJyb2xlcyI6WyJBRE1JTiJdLCJwZXJtaXNzaW9ucyI6WyJVU0VSU19DUkVBVEUiLCJVU0VSU19SRUFEIl0sImF1dGhvcml6YXRpb24iOnsicm9sZXMiOlsitBRE1JTiJdLCJwZXJtaXNzaW9ucyI6WyJVU0VSU19DUkVBVEUiLCJVU0VSU19SRUFEIl19LCJlbWFpbCI6ImFkbWluQHNwb3J0c2V2ZW50cy5jb20iLCJ1c2VybmFtZSI6ImFkbWluIiwiaWF0IjoxNzg5MjI2MjY0LCJleHAiOjE3ODkzMTI2NjR9.p3sKIQGOTfj82FHsU_6bNXHXXvP_ltekapO5NAkYy_s",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "admin@sportsevents.com",
      "roles": [
        "ADMIN"
      ]
    }
  },
  "timestamp": "2026-09-19T14:40:00Z",
  "requestId": "195eabcd-17c4-4e39-a53c-bd42cea4dac2"
}
```

---

### 1.2 Error: Credenciales Inválidas (401 Unauthorized)
**POST** `/api/v1/auth/anonymous/login`

**Request Body:**
```json
{
  "email": "admin@sportsevents.com",
  "password": "WrongPassword"
}
```

**Response Body (401 Unauthorized):**
```json
{
  "status": "error",
  "error": {
    "code": "INVALID_CREDENTIALS",
    "details": "Invalid email or password"
  },
  "timestamp": "2026-09-19T14:40:00Z",
  "requestId": "e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b"
}
```

---

## 👤 Módulo 2: Gestión Protegida de Usuarios

### 2.1 Crear Usuario Exitosamente (201 Created)
**POST** `/api/v1/users/protected/users` (o `/api/v1/users`)

**Headers:**
```http
Authorization: Bearer <jwt_con_permiso_USERS_CREATE>
Content-Type: application/json
Accept: application/json
```

**Request Body:**
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

**Response Body (201 Created):**
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
  "requestId": "f2a3b4c5-d6e7-8f9a-0b1c-2d3e4f5a6b7c"
}
```

---

### 2.2 Error: Correo Electrónico Duplicado (409 Conflict)
**POST** `/api/v1/users/protected/users`

**Request Body:**
```json
{
  "username": "otro_usuario",
  "email": "carlos.gomez@sportsevents.com",
  "password": "Password123+",
  "firstName": "Otro",
  "lastName": "Usuario"
}
```

**Response Body (409 Conflict):**
```json
{
  "status": "error",
  "error": {
    "code": "DUPLICATE_RESOURCE",
    "details": "User with email 'carlos.gomez@sportsevents.com' already exists"
  },
  "timestamp": "2026-09-19T14:40:00Z",
  "requestId": "a3b4c5d6-e7f8-9a0b-1c2d-3e4f5a6b7c8d"
}
```

---

### 2.3 Error: Permisos Insuficientes (403 Forbidden)
**POST** `/api/v1/users/protected/users`

Enviando un JWT válido pero sin el permiso `USERS_CREATE`:

**Response Body (403 Forbidden):**
```json
{
  "status": "error",
  "error": {
    "code": "ACCESS_DENIED",
    "details": "Access Denied"
  },
  "timestamp": "2026-09-19T14:40:00Z",
  "requestId": "b4c5d6e7-f8a9-0b1c-2d3e-4f5a6b7c8d9e"
}
```

---

### 2.4 Error: Error de Validación de Datos (400 Bad Request)
**POST** `/api/v1/users/protected/users`

**Request Body:**
```json
{
  "username": "",
  "email": "correo-invalido",
  "password": "123",
  "firstName": "",
  "lastName": ""
}
```

**Response Body (400 Bad Request):**
```json
{
  "status": "error",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": "Username is required; Email must be a valid email address; Password must be at least 6 characters; First name is required; Last name is required"
  },
  "timestamp": "2026-09-19T14:40:00Z",
  "requestId": "c5d6e7f8-a90b-1c2d-3e4f-5a6b7c8d9e0f"
}
```
