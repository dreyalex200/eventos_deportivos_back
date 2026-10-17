# Funcionalidad: Diagnóstico y Monitoreo (Actuator)

- **ID de Funcionalidad**: FUNC-SYS-MONITORING
- **Capa**: Anonymous / Spring Boot Actuator
- **Rutas**:
  - `GET /actuator/health`
  - `GET /actuator/health/liveness`
  - `GET /actuator/health/readiness`
  - `GET /actuator/info`
  - `GET /actuator/metrics`
  - `GET /actuator/prometheus`
- **Autenticación requerida**: Ninguna (Pública / Anónima)
- **Consumo de datos**: Ninguno
- **Producción de datos**: `application/json` / `text/plain` (Prometheus)

---

## 1. Descripción y Propósito

El subsistema de monitoreo provee verificación de liveness (supervivencia) y readiness (disponibilidad) para orquestadores de contenedores (como Docker o Kubernetes), sistemas de integración continua (CI/CD) y herramientas de observabilidad como Prometheus, Consul o Grafana.

---

## 2. Endpoints Disponibles

### A. Health Check (`GET /actuator/health`)
Verifica el estado de salud de los componentes del microservicio (conexión a la base de datos MySQL, espacio en disco y conectores auxiliares). La configuración actual expone **detalles completos** de cada componente (`show-details: always`).

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

### B. Liveness Probe (`GET /actuator/health/liveness`)
Indica si el proceso de la aplicación está vivo (running). Si retorna `DOWN`, el orquestador reinicia el contenedor.

- **URL**: `http://localhost:9000/actuator/health/liveness`
- **Código de Estado**: `200 OK` (`status: "UP"`) / `503 Service Unavailable` (`status: "DOWN"`)

---

### C. Readiness Probe (`GET /actuator/health/readiness`)
Indica si la aplicación está lista para recibir tráfico (todas las dependencias inicializadas). Si retorna `DOWN`, el balanceador deja de enviar tráfico al pod.

- **URL**: `http://localhost:9000/actuator/health/readiness`
- **Código de Estado**: `200 OK` (`status: "UP"`) / `503 Service Unavailable` (`status: "DOWN"`)

---

### D. Información de Compilación y Entorno (`GET /actuator/info`)
Expone metadatos de la aplicación tales como nombre del servicio, versión, perfil activo de Spring y detalles de compilación de Gradle.

- **URL**: `http://localhost:9000/actuator/info`
- **Código de Estado**: `200 OK`

---

### E. Métricas (`GET /actuator/metrics`)
Expone métricas de la JVM y de la aplicación (memoria, hilos, solicitudes HTTP, etc.) en formato JSON de Spring Boot Actuator.

- **URL**: `http://localhost:9000/actuator/metrics`
- **Código de Estado**: `200 OK`

---

### F. Métricas Prometheus (`GET /actuator/prometheus`)
Expone las métricas en formato de texto compatible con el scraping de **Prometheus** (Micrometer). Incluye la etiqueta de aplicación `application: api_events_sports` en todas las métricas exportadas.

- **URL**: `http://localhost:9000/actuator/prometheus`
- **Código de Estado**: `200 OK`
- **Content-Type**: `text/plain; version=0.0.4; charset=utf-8`

---

## 3. Configuración de Seguridad

En [`SecurityConfig.java`](file:///d:/proyecto/src/back/api_events_sports/src/main/java/com/drey/eventsSports/infrastructure/config/SecurityConfig.java), las rutas `/actuator/**` están explícitamente permitidas de forma anónima:

```java
.requestMatchers("/actuator/**").permitAll()
```

Esto garantiza que herramientas de monitoreo externas o balanceadores de carga puedan consultar el estado del microservicio sin requerir inyección de credenciales JWT.

---

## 4. Configuración en `application.yml`

Los endpoints activos y su comportamiento se configuran en [`application.yml`](file:///d:/proyecto/src/back/api_events_sports/src/main/resources/application.yml):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics, liveness, readiness
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        enabled: true
  prometheus:
    metrics:
      export:
        enabled: true
```

- **`show-details: always`**: Los detalles de cada subsistema de salud (DB, disco, etc.) son visibles sin autenticación.
- **`probes.enabled: true`**: Habilita las sub-rutas `/actuator/health/liveness` y `/actuator/health/readiness` para integración con Kubernetes / Docker.
- **Etiqueta `application`**: Todas las métricas exportadas a Prometheus llevan la etiqueta `application="api_events_sports"`.

