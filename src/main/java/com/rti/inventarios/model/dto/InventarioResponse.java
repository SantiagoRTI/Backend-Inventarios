package com.rti.inventarios.model.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de información de inventario.
 * 
 * Incluye referencias al inspector y centro de costos asignados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioResponse {

    /**
     * Identificador único del inventario
     */
    private Long id;

    /**
     * Nombre descriptivo del inventario
     */
    private String nombre;

    /**
     * Código único del inventario
     */
    private String codigo;

    /**
     * Estado actual del inventario
     */
    private String estado;

    /**
     * Información del inspector asignado
     */
    private UsuarioResponse inspector;

    /**
     * Información del centro de costos asociado
     */
    private CentroCostosResponse centroCostos;

    /**
     * Fecha de creación del inventario
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de finalización del inventario
     */
    private LocalDateTime fechaFinalizacion;

    /**
     * Indica si el inventario está activo
     */
    private Boolean activo;
}
