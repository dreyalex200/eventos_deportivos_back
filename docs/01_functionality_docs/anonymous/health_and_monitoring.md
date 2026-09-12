# Funcionalidad: Diagnóstico y Monitoreo (Actuator)

- **ID de Funcionalidad**: FUNC-SYS-MONITORING
- **Capa**: Anonymous / Spring Boot Actuator
- **Rutas**: 
  - `GET /actuator/health`
  - `GET /actuator/info`
- **Autenticación requerida**: Ninguna (Pública / Anónima)
- **Consumo de datos**: Ninguno
- **Producción de datos**: `application/json`

---

## 1. Descripción y Propósito

El subsistema de monitoreo provee verificación de liveness (supervivencia) y readiness (disponibilidad) para orquestadores de contenedores (como Docker o Kubernetes), sistemas de integración continua (CI/CD) y herramientas de observabilidad como Prometheus, Consul o Grafana.

---

## 2. Endpoints Disponibles

### A. Health Check (`GET /actuator/health`)
Verifica el estado de salud de los componentes del microservicio (conexión a la base de datos MySQL, espacio en disco y conectores auxiliares).

- **URL**: `http://localhost:9000/actuator/health`
- **Códigos de Estado**:
  - `200 OK`: Todos los servicios requeridos se encuentran disponibles (`status: "UP"`).
  - `503 Service Unavailable`: Al menos un componente dependiente crítico se encuentra fuera de servicio (`status: "DOWN"`).

#### Ejemplo de Respuesta (UP):
```json
{
  "status": "UP"
}
```

---

### B. Información de Compilación y Entorno (`GET /actuator/info`)
Expone metadatos de la aplicación tales como nombre del servicio, versión, perfil activo de Spring y detalles de compilación de Gradle.

- **URL**: `http://localhost:9000/actuator/info`
- **Código de Estado**: `200 OK`

---

## 3. Configuración de Seguridad

En `SecurityConfig.java`, las rutas `/actuator/**` están explícitamente permitidas de forma anónima:

```java
.requestMatchers("/actuator/**").permitAll()
```

Esto garantiza que herramientas de monitoreo externas o balanceadores de carga puedan consultar el estado del microservicio sin requerir inyección de credenciales JWT.
