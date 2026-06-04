package com.rti.inventarios;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Inventarios RTI Backend.
 * 
 * Esta aplicación proporciona una API REST para la gestión de inventarios de activos,
 * permitiendo la carga inicial de activos por administradores, registro de activos por
 * inspectores en campo, y el cruce de información entre ambas fuentes.
 * 
 * Características principales:
 * - Autenticación JWT con roles Administrador e Inspector
 * - CRUD de usuarios e inventarios
 * - Carga masiva de activos desde Excel
 * - Registro de activos en campo
 * - Cruce de información con estados: CRUCE_NORMAL, EDITADO, SOBRANTE, FALTANTE
 * - Generación de reportes en Excel
 * 
 * Tecnologías utilizadas:
 * - Java 17 LTS
 * - Spring Boot 3
 * - Spring Security + JWT
 * - MySQL 8.x
 * - Apache POI (manejo de Excel)
 * - Flyway (migraciones de base de datos)
 */
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Inventarios RTI API",
                version = "1.0.0",
                description = "API REST para la plataforma de inventarios RTI. " +
                              "Expone servicios de autenticación, gestión de usuarios e inventarios, " +
                              "carga masiva por Excel, inspección de activos y cruce de información."
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class InventariosRtiApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(InventariosRtiApplication.class, args);
        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════╗\n" +
                "║                                                       ║\n" +
                "║         Inventarios RTI Backend - INICIADO            ║\n" +
                "║                                                       ║\n" +
                "║   Swagger UI: http://localhost:8050/swagger-ui.html  ║\n" +
                "║   API Docs:   http://localhost:8050/api-docs         ║\n" +
                "║                                                       ║\n" +
                "╚═══════════════════════════════════════════════════════╝\n");
    }
}
