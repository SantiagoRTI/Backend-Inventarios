package com.rti.inventarios.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de error estandarizadas en la API.
 * 
 * Proporciona información detallada sobre errores que ocurren durante
 * el procesamiento de peticiones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * Timestamp del error
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Código de estado HTTP
     */
    private Integer status;

    /**
     * Mensaje de error principal
     */
    private String error;

    /**
     * Detalle adicional del error
     */
    private String detalle;

    /**
     * Lista de errores de validación (opcional)
     */
    private List<String> errores;

    /**
     * Ruta de la petición que generó el error
     */
    private String path;
}
