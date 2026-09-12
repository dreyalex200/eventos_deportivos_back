# Ejemplos de Request y Response: api_messaging

Este documento proporciona ejemplos detallados de las peticiones y respuestas para los endpoints principales, siguiendo el estándar de la arquitectura.

> **Nota sobre Errores:** Las respuestas de error 404 (Not Found) se reservan exclusivamente para rutas inexistentes. Si un recurso (mensaje, plantilla, etc.) no es encontrado en una consulta válida, el servidor devolverá un error **400 Bad Request** con el código interno `bad_request`.

---

## 📱 Notas sobre Canales Móviles (SMS / WhatsApp)

### Normalización de Números
El microservicio normaliza automáticamente los números de teléfono al formato **E.164** requerido por proveedores como Bird.com:
- Se eliminan espacios, guiones y caracteres no numéricos.
- **Lógica para Colombia**: Si el número tiene 10 dígitos y empieza por `3`, se añade automáticamente el prefijo `+57`.
- **Formato Global**: Si el número no incluye el símbolo `+`, el sistema lo añade asumiendo que el usuario ya incluyó el código de país.

### Campo `content`
- Para **SMS**, el campo `content` es obligatorio a menos que se use una plantilla.
- Para **WhatsApp**, el campo `content` es **opcional**, ya que el mensaje se construye en el proveedor a partir de la `template_id` y las `variables`.

---

## 📡 Módulo 1: Gestión de Mensajes

## 🔐 Autenticación (Headers)

Para consumir el API puedes usar cualquiera de estos esquemas:

### A) Token de Usuario (JWT)
```http
Authorization: Bearer <jwt>
```

### B) Token de Aplicación (Microservicio a Microservicio)
```http
X-App-Token: <app_token>
X-Scope-Id: <uuid>
```

### 1.1 Enviar Mensaje Multicanal (RF-01)
**POST** `/api/v1/messaging/messages`

**Request Body:**
```json
{
  "channel": "EMAIL",
  "to": "usuario@ejemplo.com",
  "cc": "copia@ejemplo.com",
  "from": {
    "email": "no-reply@drey.com",
    "name": "drey"
  },
  "subject": "Bienvenida al Sistema",
  "templateCode": "USER_REGISTER_SUCCESS_EMAIL",
  "variables": {
    "email_user": "usuario@ejemplo.com"
  },
  "attachments": [
    {
      "filename": "bienvenida.pdf",
      "content_type": "application/pdf",
      "url": "https://storage.ecosystem.com/docs/123.pdf"
    }
  ],
  "priority": "HIGH",
  "metadata": {
    "source": "web_registration"
  }
}
```

**Response (202 Accepted):**
```json
{
  "status": 202,
  "message": "Mensaje aceptado para procesamiento asíncrono",
  "data": {
    "message_id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "QUEUED",
    "estimated_delivery": "2026-03-08T15:00:05Z",
    "check_status_url": "/api/v1/messaging/messages/550e8400-e29b-41d4-a716-446655440000/status",
    "_links": {
      "self": "/api/v1/messaging/messages/550e8400-e29b-41d4-a716-446655440000",
      "status": "/api/v1/messaging/messages/550e8400-e29b-41d4-a716-446655440000/status",
      "events": "/api/v1/messaging/messages/550e8400-e29b-41d4-a716-446655440000/events"
    }
  },
  "timestamp": "2026-03-08T15:00:00Z"
}
```

### 1.1.1 Enviar Email (OTP / 2FA) por Plantilla (RF-02.1)
**POST** `/api/v1/messaging/messages/email`

**Headers (opción B - token de app):**
```http
X-App-Token: <app_token>
X-Scope-Id: 1703a446-f76d-4de6-bcba-c564826fcd95
```

**Request Body:**
```json
{
  "templateCode": "CODE_TWO_FACTOR_EMAIL",
  "to": "usuario@ejemplo.com",
  "from": {
    "email": "no-reply@drey.com",
    "name": "drey"
  },
  "variables": {
    "email_user": "usuario@ejemplo.com",
    "otp_code": "123456",
    "expiration_minutes": "10",
    "ip_address": "127.0.0.1",
    "device_id": "Chrome / Windows"
  },
  "priority": "HIGH"
}
```

### 1.1.2 Enviar Email (Recuperación de Contraseña) por Plantilla (RF-02.1)
**POST** `/api/v1/messaging/messages/email`

**Headers (opción A - JWT de usuario):**
```http
Authorization: Bearer <jwt>
```

Nota:
- El `scopeId` se obtiene del JWT. Si se envía en el body, debe coincidir con el token.

**Request Body:**
```json
{
  "templateCode": "FORGOT_PASSWORD_EMAIL",
  "to": "usuario@ejemplo.com",
  "from": {
    "email": "no-reply@drey.com",
    "name": "drey"
  },
  "variables": {
    "full_name_user": "María Gómez",
    "email_user": "usuario@ejemplo.com",
    "reset_link": "https://app.ejemplo.com/reset?token=abc123",
    "expiration_time": "2"
  },
  "priority": "HIGH"
}
```

### 1.1.3 Enviar Email (Registro de Usuario) por Plantilla (RF-02.1)
**POST** `/api/v1/messaging/messages/email`

**Headers (opción B - token de app):**
```http
X-App-Token: <app_token>
X-Scope-Id: 1703a446-f76d-4de6-bcba-c564826fcd95
```

**Request Body:**
```json
{
  "templateCode": "USER_REGISTER_EMAIL",
  "to": "usuario@ejemplo.com",
  "from": {
    "email": "no-reply@drey.com",
    "name": "drey"
  },
  "variables": {
    "email_user": "usuario@ejemplo.com",
    "register_link": "https://app.ejemplo.com/registro?token=abc123",
    "expiration_time": "2"
  },
  "priority": "HIGH"
}
```

### 1.1.4 Enviar Email (Registro Exitoso) por Plantilla (RF-02.1)
**POST** `/api/v1/messaging/messages/email`

**Headers (opción A - JWT de usuario):**
```http
Authorization: Bearer <jwt>
```

**Request Body:**
```json
{
  "templateCode": "USER_REGISTER_SUCCESS_EMAIL",
  "to": "usuario@ejemplo.com",
  "from": {
    "email": "no-reply@drey.com",
    "name": "drey"
  },
  "variables": {
    "full_name_user": "María Gómez",
    "email_user": "usuario@ejemplo.com"
  },
  "priority": "HIGH"
}
```

### 1.2 Enviar SMS (Bird.com)
**POST** `/api/v1/messaging/messages`

**Request Body:**
```json
{
  "channel": "SMS",
  "to": "3003460877",
  "content": "Este es un mensaje de prueba enviado vía Bird.com",
  "priority": "HIGH"
}
```
*Nota: El número se convertirá automáticamente a `+573003460877`.*

### 1.3 Enviar WhatsApp vía Plantilla (Bird.com)
**POST** `/api/v1/messaging/messages`

**Request Body:**
```json
{
  "channel": "WHATSAPP",
  "to": "3003460877",
  "templateId": "838b3e8b-1d50-4bec-8b6a-03df1cfca3ab",
  "variables": {
    "otp": "123456"
  },
  "priority": "HIGH"
}
```
*Nota: Para WhatsApp, el `content` no es necesario si se proporciona un `template_id` válido configurado en Bird.*

### 1.4 Seguimiento de Estado (RF-04)
**GET** `/api/v1/messaging/messages/{id}/status`

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "message_id": "550e8400-e29b-41d4-a716-446655440000",
    "status": "DELIVERED",
    "recipient": "usuario@ejemplo.com",
    "last_update": "2026-03-08T15:01:20Z",
    "history": [
      {
        "status": "PENDING",
        "created_at": "2026-03-08T15:00:00Z"
      },
      {
        "status": "SENT",
        "created_at": "2026-03-08T15:00:10Z"
      },
      {
        "status": "DELIVERED",
        "created_at": "2026-03-08T15:01:20Z"
      }
    ]
  }
}
```

**Response (400 Bad Request - Mensaje no encontrado):**
```json
{
  "status": "error",
  "error": {
    "code": "bad_request",
    "details": "El mensaje con el id proporcionado no existe"
  },
  "timestamp": "2026-03-10T17:39:39Z",
  "requestId": "22fb1ef6-f785-42cf-b466-6b24f5ced9dd"
}
```

**Response (401 Unauthorized - Token inválido):**
```json
{
  "status": "error",
  "error": {
    "code": "token_invalido",
    "details": "El token proporcionado no es válido o ha expirado (o faltan headers requeridos para autenticación por app token)"
  },
  "timestamp": "2026-03-10T17:37:57Z",
  "requestId": "3c791f0b-9572-4673-aa49-3a54895fa29e"
}
```

**Response (404 Not Found - Ruta inexistente):**
```json
{
  "status": "error",
  "error": {
    "code": "ROUTE_NOT_FOUND",
    "details": "no route found for GET /"
  }
}
```

---

## 📄 Módulo 2: Gestión de Plantillas

### 2.1 Crear Plantilla (RF-09)
**POST** `/api/v1/messaging/templates`

Permisos:
- `MESSAGING_TEMPLATES_WRITE` para crear/actualizar/eliminar/aprobar/duplicar
- `MESSAGING_TEMPLATES_READ` para consultar

**Request Body:**
```json
{
  "scope_id": "550e8400-e29b-41d4-a716-446655440001",
  "code": "WELCOME_EMAIL",
  "name": "Email de Bienvenida Estándar",
  "channel": "EMAIL",
  "subject": "¡Hola {{nombre}}! Bienvenido a {{empresa}}",
  "content": {
    "html": "<h1>Bienvenido {{nombre}}</h1><p>Estamos felices de tenerte en {{empresa}}.</p>",
    "text": "Bienvenido {{nombre}}. Estamos felices de tenerte en {{empresa}}."
  },
  "variables": {
    "nombre": "Nombre del usuario",
    "empresa": "Nombre de la compañía"
  },
  "is_active": true
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "data": {
    "template_id": "550e8400-e29b-41d4-a716-446655440002",
    "code": "WELCOME_EMAIL",
    "version": 1,
    "isActive": true
  }
}
```

---

## 🔗 Módulo 4: Webhooks

### 4.1 Registrar Webhook (RF-31)
**POST** `/api/v1/messaging/webhooks`

Permisos:
- `MESSAGING_WEBHOOKS_WRITE` para registrar/actualizar/eliminar/probar
- `MESSAGING_WEBHOOKS_READ` para consultar

**Request Body:**
```json
{
  "scope_id": "550e8400-e29b-41d4-a716-446655440001",
  "url": "https://cliente.com/webhooks/messaging",
  "secret_token": "mi_secreto_super_seguro",
  "events": ["SENT", "DELIVERED", "FAILED"],
  "is_active": true
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "data": {
    "webhook_id": 1,
    "scope_id": "550e8400-e29b-41d4-a716-446655440001",
    "url": "https://cliente.com/webhooks/messaging",
    "isActive": true
  }
}
```

---

## 📊 Módulo 5: Dashboard

### 5.1 Resumen de Métricas (RF-37)
**GET** `/api/v1/messaging/dashboard/summary/550e8400-e29b-41d4-a716-446655440001`

Permisos:
- `MESSAGING_DASHBOARD_READ` para consultar/exportar

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "totalMessages": 15432,
    "sent": 15200,
    "delivered": 14800,
    "failed": 232,
    "deliveryRate": 0.959,
    "byStatus": {
      "SENT": 15200,
      "DELIVERED": 14800,
      "FAILED": 232,
      "QUEUED": 0,
      "PENDING": 0
    },
    "byChannel": {}
  }
}
```
