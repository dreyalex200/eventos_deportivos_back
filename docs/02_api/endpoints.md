# API Endpoints: Microservicio api_messaging

Este documento detalla los endpoints disponibles para interactuar con el microservicio de mensajería multicanal.

## 🔐 Autenticación

Este microservicio acepta dos tipos de autenticación:

### 1) Token de Usuario (JWT)
- Header: `Authorization: Bearer <jwt>`
- Origen: generado por el servicio de autenticación.
- Uso: recomendado para tráfico usuario/app.

### 2) Token de Aplicación (Microservicio a Microservicio)
- Header: `X-App-Token: <app_token>`
- Scope: `X-Scope-Id: <uuid>` (obligatorio).
- Uso: recomendado para integraciones internas (backends/servicios).

Reglas:
- Si se envía `Authorization: Bearer ...`, el token debe ser válido. No hay fallback a `X-App-Token` si el JWT es inválido.
- Si no se envía `Authorization`, se valida `X-App-Token` y `X-Scope-Id`.

## 🔑 Autorización por Permisos
- Plantillas (`/api/v1/messaging/templates/*`)
  - Lectura: `MESSAGING_TEMPLATES_READ`
  - Escritura (crear/actualizar/eliminar/aprobar/duplicar): `MESSAGING_TEMPLATES_WRITE`
- Webhooks (`/api/v1/messaging/webhooks/*`)
  - Lectura: `MESSAGING_WEBHOOKS_READ`
  - Escritura (registrar/actualizar/eliminar/probar): `MESSAGING_WEBHOOKS_WRITE`
- Dashboard (`/api/v1/messaging/dashboard/*`)
  - Lectura (consultar/exportar): `MESSAGING_DASHBOARD_READ`

## 📡 Módulo 1: Gestión de Mensajes


| Método | Endpoint | Descripción | RF |
|--------|----------|-------------|----|
| POST | `/api/v1/messaging/messages` | Envío de mensaje multicanal (general) | RF-01 |
| POST | `/api/v1/messaging/messages/email` | Envío optimizado para Email | RF-02.1 |
| POST | `/api/v1/messaging/messages/sms` | Envío optimizado para SMS | RF-02.2 |
| POST | `/api/v1/messaging/messages/whatsapp` | Envío optimizado para WhatsApp | RF-02.3 |
| POST | `/api/v1/messaging/messages/push` | Envío optimizado para Push | RF-02.4 |
| GET | `/api/v1/messaging/messages/{id}` | Consulta detalle de un mensaje | RF-03.1 |
| GET | `/api/v1/messaging/messages` | Listado paginado de mensajes | RF-03.2 |
| GET | `/api/v1/messaging/messages/{id}/status` | Seguimiento de estado y timeline | RF-04 |
| GET | `/api/v1/messaging/messages/{id}/events` | Historial completo de eventos | RF-05 |
| POST | `/api/v1/messaging/messages/{id}/cancel` | Cancelar mensaje programado | RF-06 |
| POST | `/api/v1/messaging/messages/{id}/retry` | Reintentar mensaje fallido | RF-07 |
| POST | `/api/v1/messaging/messages/{id}/resend` | Reenviar mensaje (duplicar) | RF-08 |

---

## 📄 Módulo 2: Gestión de Plantillas

| Método | Endpoint | Descripción | RF |
|--------|----------|-------------|----|
| POST | `/api/v1/messaging/templates` | Crear nueva plantilla | RF-09 |
| GET | `/api/v1/messaging/templates/{id}` | Obtener detalle de plantilla | RF-10.1 |
| GET | `/api/v1/messaging/templates` | Listar plantillas del scope | RF-10.2 |
| PUT | `/api/v1/messaging/templates/{id}` | Actualizar plantilla (nueva versión) | RF-11 |
| DELETE | `/api/v1/messaging/templates/{id}` | Desactivar plantilla | RF-12 |
| POST | `/api/v1/messaging/templates/{id}/approve` | Aprobar plantilla para uso | RF-13 |
| POST | `/api/v1/messaging/templates/{id}/preview` | Previsualizar con variables | RF-14 |
| POST | `/api/v1/messaging/templates/{id}/validate` | Validar variables contra esquema | RF-15 |
| POST | `/api/v1/messaging/templates/{id}/duplicate` | Duplicar plantilla existente | RF-16 |

---

## ⚙️ Módulo 3: Proveedores y Scopes

| Método | Endpoint | Descripción | RF |
|--------|----------|-------------|----|
| GET | `/api/v1/messaging/providers` | Listar proveedores soportados | RF-17 |
| POST | `/api/v1/messaging/scopes/{sid}/providers` | Configurar proveedor para scope | RF-18 |
| GET | `/api/v1/messaging/providers/{code}/health` | Salud y Circuit Breaker de proveedor | RF-22 |
| GET | `/api/v1/messaging/scopes/me` | Información del scope actual | RF-25 |
| POST | `/api/v1/messaging/scopes/{sid}/limits` | Configurar cuotas y límites | RF-26 |
| GET | `/api/v1/messaging/scopes/{sid}/usage` | Consultar consumo actual | RF-27 |

---

## 🔗 Módulo 4: Webhooks y Eventos

| Método | Endpoint | Descripción | RF |
|--------|----------|-------------|----|
| POST | `/api/v1/messaging/webhooks` | Registrar nuevo webhook | RF-31 |
| GET | `/api/v1/messaging/webhooks` | Listar webhooks configurados | RF-32 |
| PUT | `/api/v1/messaging/webhooks/{id}` | Actualizar configuración de webhook | RF-33 |
| DELETE | `/api/v1/messaging/webhooks/{id}` | Eliminar webhook | RF-34 |
| POST | `/api/v1/messaging/webhooks/{id}/test` | Enviar evento de prueba | RF-35 |
| GET | `/api/v1/messaging/webhooks/{id}/deliveries` | Historial de entregas | RF-36 |

---

## 📊 Módulo 5: Dashboard y Métricas

| Método | Endpoint | Descripción | RF |
|--------|----------|-------------|----|
| GET | `/api/v1/messaging/dashboard/summary/{scopeId}` | Resumen de métricas clave por scope | RF-37 |
| GET | `/api/v1/messaging/dashboard/realtime` | Métricas en tiempo real | RF-40 |
| GET | `/api/v1/messaging/dashboard/export` | Exportar reportes (CSV/JSON) | RF-41 |

---

## 🛠️ Módulo 6: Mantenimiento y Seguridad

| Método | Endpoint | Descripción | RF |
|--------|----------|-------------|----|
| GET | `/api/v1/messaging/health` | Health Check detallado | RF-54 |
| GET | `/api/v1/messaging/actuator/prometheus` | Métricas formato Prometheus | RF-56 |
| GET | `/api/v1/messaging/audit-logs` | Consultar logs de auditoría | RF-50 |

---

## 📱 Formato de Números Telefónicos (SMS / WhatsApp)

El sistema soporta el envío de números en formato local o internacional para Colombia:
- **Formato Local (10 dígitos)**: `3003460877` se normaliza a `+573003460877`.
- **Formato Internacional sin símbolo**: `573003460877` se normaliza a `+573003460877`.
- **Estándar E.164**: Siempre se recomienda enviar el número con el prefijo `+` y el código de país.

---

## 🛠️ Estándar de Errores

El microservicio utiliza un formato estandarizado para las respuestas de error (4xx y 5xx):

- **400 Bad Request:** Se devuelve cuando la petición es inválida o un recurso solicitado (mensaje, plantilla, etc.) **no existe**.
- **401 Unauthorized:** Se devuelve cuando el token es inválido, expirado o no se proporciona.
- **404 Not Found:** Reservado exclusivamente para **rutas inexistentes** o archivos no encontrados.

Para más detalles, consulte [Códigos de Error](codigos-error.md) y [Ejemplos de Request/Response](ejemplos-request-response.md).

---

## ✉️ Plantillas Email del Sistema (Template Codes)

Las siguientes plantillas se pueden enviar usando `POST /api/v1/messaging/messages/email` con `templateCode`:

| templateCode | Descripción | Variables requeridas |
|---|---|---|
| `CODE_TWO_FACTOR_EMAIL` | Código de verificación (2FA) | `email_user`, `otp_code`, `expiration_minutes`, `ip_address`, `device_id` |
| `FORGOT_PASSWORD_EMAIL` | Recuperación de contraseña | `full_name_user`, `email_user`, `reset_link`, `expiration_time` |
| `USER_REGISTER_EMAIL` | Continuar registro de usuario | `email_user`, `register_link`, `expiration_time` |
| `USER_REGISTER_SUCCESS_EMAIL` | Confirmación de registro exitoso | `full_name_user`, `email_user` |

Notas:
- En los endpoints de envío de mensajes, el `scopeId` se toma del método de autenticación (JWT o headers). Si se envía en el body, debe coincidir con el scope autenticado.
- `assets_base_url` y `year` se inyectan automáticamente durante el render del template.
