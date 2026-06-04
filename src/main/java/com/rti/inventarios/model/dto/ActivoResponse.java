package com.rti.inventarios.model.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de información de activo.
 * 
 * Incluye todos los datos del activo y su origen (ADMINISTRADOR o INSPECTOR).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivoResponse {

    /**
     * Identificador del registro
     */
    private Long id;

    /**
     * ID del inventario
     */
    private Long inventarioId;

    /**
     * Identificador del activo
     */
    private String idActivo;

    /**
     * Etiqueta o código de barras
     */
    private String etiqueta;

    /**
     * Descripción del activo
     */
    private String descripcion;

    /**
     * Marca del activo
     */
    private String marca;

    /**
     * Número de serie
     */
    private String serial;

    /**
     * Modelo del activo
     */
    private String modelo;

    /**
     * Persona responsable
     */
    private String responsable;

    /**
     * Ciudad donde se encuentra
     */
    private String ciudad;

    /**
     * Estado físico del activo
     */
    private String estado;

    /**
     * Origen del registro (ADMINISTRADOR o INSPECTOR)
     */
    private String origen;

    /**
     * Fecha de registro
     */
    private LocalDateTime fechaCreacion;
}
