# 🚀 Despliegue del Microservicio API Messaging

Este documento contiene las instrucciones para desplegar el microservicio `api_messaging` utilizando Docker y Docker Compose, tanto para entornos locales como para producción (ej: CapRover).

## 1. Requisitos Previos

-   Docker y Docker Compose instalados.
-   Acceso a una instancia de PostgreSQL y RabbitMQ (pueden ser externas).
-   Un archivo `.env` en la raíz del proyecto (`api_messaging/.env`) para la configuración local.

---

## 2. Despliegue en Entorno Local

Para levantar el microservicio y su proxy Nginx en tu máquina local.

1.  **Configurar Variables de Entorno**:
    -   Asegúrate de tener un archivo `.env` en la raíz del proyecto (`api_messaging/.env`).
    -   Este archivo debe contener las variables para conectarse a tus instancias locales de PostgreSQL y RabbitMQ. Puedes usar el archivo `.deploy/.env.example` como plantilla.

2.  **Levantar los Contenedores**:
    -   Navega a la carpeta `.deploy`:
        ```bash
        cd .deploy
        ```
    -   Ejecuta Docker Compose:
        ```bash
        docker-compose up --build
        ```
    -   El comando construirá la imagen del servicio (`propietarios/api-messaging:latest`) y levantará los servicios `api_messaging` y `nginx`.

3.  **Acceso**:
    -   El microservicio estará disponible en `http://localhost:80`.
    -   Nginx actuará como reverse proxy, redirigiendo todo el tráfico al contenedor de la aplicación.

---

## 3. Despliegue en Producción (CapRover)

El proyecto está diseñado para ser 100% configurable a través de variables de entorno, lo cual es ideal para plataformas como CapRover.

1.  **Preparar la Aplicación en CapRover**:
    -   Crea una nueva aplicación en tu instancia de CapRover.
    -   En la sección **Deployment**, selecciona el método "Deploy from local .tar file".

2.  **Configurar Variables de Entorno**:
    -   Ve a la pestaña **App Configs** de tu aplicación en CapRover.
    -   Utiliza el archivo **`.deploy/.env.example`** como guía para saber qué variables necesitas.
    -   Agrega **una por una** las variables de entorno con sus valores de producción (ej: `SERVER_PORT`, `SPRING_DATASOURCE_URL`, `SPRING_RABBITMQ_HOST`, `APP_SECURITY_JWT_SECRET`, etc.).

3.  **Desplegar**:
    -   Desde la raíz de tu proyecto local, ejecuta el comando de despliegue de CapRover:
        ```bash
        caprover deploy
        ```
    -   Sigue las instrucciones para seleccionar tu aplicación y la rama a desplegar. CapRover empaquetará tu código, lo enviará al servidor y construirá la imagen Docker usando el `Dockerfile` del proyecto.

4.  **Habilitar HTTPS**:
    -   Una vez desplegado, no olvides ir a la sección **HTTP Settings** en CapRover y habilitar HTTPS para tu dominio.

---

## 4. Estructura del Despliegue

-   **`Dockerfile`**: Define un multi-stage build para crear una imagen de Spring Boot ligera y optimizada.
-   **`docker-compose.yml`**: Orquesta los servicios `api_messaging` (la aplicación Java) y `nginx` (el reverse proxy).
-   **`nginx.conf`**: Configuración de Nginx que redirige todo el tráfico del puerto 80 al puerto 9000 del contenedor de la aplicación.
-   **`.env.example`**: Plantilla con todas las variables de entorno necesarias para configurar la aplicación.
