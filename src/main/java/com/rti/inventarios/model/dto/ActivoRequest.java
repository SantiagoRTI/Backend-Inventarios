package com.rti.inventarios.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para la creación y actualización de activos por el inspector.
 * 
 * Contiene todos los campos necesarios para registrar un activo en campo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivoRequest {

    /**
     * ID del inventario al que pertenece el activo
     */
    @NotNull(message = "El ID del inventario es obligatorio")
    private Long inventarioId;

    /**
     * Identificador único del activo
     */
    @NotBlank(message = "El ID del activo es obligatorio")
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
}
