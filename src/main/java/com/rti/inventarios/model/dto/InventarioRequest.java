package com.rti.inventarios.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO para la creación y actualización de inventarios.
 * 
 * Contiene la información necesaria para registrar o modificar un inventario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioRequest {

    /**
     * Nombre descriptivo del inventario
     */
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    /**
     * Código único del inventario
     */
    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    /**
     * Estado del inventario (Ejecución, Finalizado, etc.)
     */
    private String estado;

    /**
     * ID del inspector asignado al inventario
     */
    private Long inspectorId;

    /**
     * ID del centro de costos asociado
     */
    private Long centroCostosId;
}
