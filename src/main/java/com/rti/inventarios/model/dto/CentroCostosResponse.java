package com.rti.inventarios.model.dto;

import lombok.*;

/**
 * DTO para la respuesta de información de centro de costos.
 * 
 * Representa la información de un centro de costos o ubicación física.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CentroCostosResponse {

    /**
     * Identificador único del centro de costos
     */
    private Long id;

    /**
     * Código único del centro
     */
    private String codigo;

    /**
     * Nombre del centro de costos
     */
    private String nombre;

    /**
     * Ciudad donde se ubica
     */
    private String ciudad;

    /**
     * Dirección física
     */
    private String direccion;

    /**
     * Indica si está activo
     */
    private Boolean activo;
}
