# ❌ Códigos de Error - api_messaging

Este documento describe los códigos de error estándar devueltos por la API de mensajería.

## 🚦 Errores HTTP Comunes

| Código HTTP | Código Interno | Descripción |
|-------------|----------------|-------------|
| **400** | `bad_request` | Petición inválida o recurso no encontrado (ej: ID inexistente). |
| **401** | `token_invalido` | Token JWT inválido, expirado o ausente. |
| **403** | `forbidden` | Token válido pero sin permisos para el scope o acción. |
| **404** | `not_found` | Ruta (endpoint) o archivo físico no encontrado. |
| **409** | `quota_exceeded` | Se ha superado el límite de mensajes permitido. |
| **422** | `provider_error` | El proveedor externo rechazó el mensaje. |
| **500** | `internal_error` | Error inesperado en el servidor o infraestructura. |

## 🛠️ Errores Específicos del Dominio

### Mensajería
- `channel_not_supported`: El canal solicitado no está configurado.
- `no_active_provider`: No hay proveedores activos o disponibles para el canal.
- `template_render_error`: Error al reemplazar las variables en la plantilla.
- `attachment_too_large`: El archivo adjunto supera el límite permitido (25MB).

### Configuración
- `scope_not_found`: El scope indicado en el token no está registrado.
- `invalid_provider_config`: La configuración del proveedor no supera la validación.

### Seguridad
- `scope_mismatch`: El scope enviado (header o body) no coincide con el scope autenticado.

Notas:
- Para autenticación por token de aplicación, `X-App-Token` y `X-Scope-Id` son requeridos. Si faltan o son inválidos, se responde `401 token_invalido`.
