package com.drey.eventsSports.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {
    // La configuración de Resilience4j se maneja preferiblemente vía application.yml
    // o mediante beans específicos de CircuitBreakerRegistry si se requiere personalización programática.
    // He eliminado las dependencias de spring-cloud-circuitbreaker que no están en el build.gradle.
}
