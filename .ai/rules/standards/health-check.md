# Health Check Standard
**Status**: Active

## Overview
Every microservice within the Arcadion Ecosystem MUST implement a Health Check endpoint. This endpoint is critical for container orchestration (e.g., Docker, Kubernetes, CapRover), monitoring, and load balancing.

## Endpoint Specification
- **Method**: `GET`
- **Path**: `/health`
- **Visibility**: Public (No JWT required)

## Response Contract
The response must follow the standard envelope pattern.

### Success Response (200 OK)
When the service is fully operational and database connectivity is established:
```json
{
  "data": {
    "status": "healthy",
    "service": "service_name",
    "timestamp": "2026-07-31T21:56:31Z"
  },
  "meta": null
}
```

### Failure Response (503 Service Unavailable)
If the service is running but cannot connect to essential dependencies (e.g., the database):
```json
{
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "Service is degraded or unable to connect to the database"
  }
}
```

## Business Rules
- The endpoint must NOT perform any state modifications.
- The endpoint must attempt a lightweight ping to the primary database to verify connectivity.
- No heavy computations should be performed.
- Response time should be < 50ms.
