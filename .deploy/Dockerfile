# --- Etapa de Build ---
# Usamos una imagen de Gradle con JDK 21 para construir el proyecto
FROM gradle:8.13-jdk21 AS build

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos los archivos de build de Gradle
COPY build.gradle settings.gradle gradlew ./ 
COPY gradle ./gradle

# Copiamos el código fuente de la aplicación
COPY src ./src

# Damos permisos de ejecución al script de Gradle
RUN chmod +x ./gradlew

# Ejecutamos el build de Spring Boot para generar el JAR
# Usamos --no-daemon para evitar problemas en entornos de CI/CD
RUN ./gradlew bootJar --no-daemon

# --- Etapa de Runtime ---
# Usamos una imagen ligera de Eclipse Temurin con JRE 21 para ejecutar la aplicación
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos el JAR generado desde la etapa de build
COPY --from=build /app/build/libs/*.jar app.jar

# Exponemos el puerto en el que corre la aplicación Spring Boot
EXPOSE 9000

# Comando para ejecutar la aplicación
# Se pueden pasar variables de entorno de Spring Boot aquí si es necesario
ENTRYPOINT ["java", "-jar", "app.jar"]
