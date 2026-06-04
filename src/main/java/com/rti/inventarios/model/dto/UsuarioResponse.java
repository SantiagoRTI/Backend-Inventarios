package com.rti.inventarios.model.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de información de usuario.
 * 
 * No incluye datos sensibles como la contraseña.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    /**
     * Identificador único del usuario
     */
    private Long id;

    /**
     * Nombre completo del usuario
     */
    private String nombre;

    /**
     * Correo electrónico del usuario
     */
    private String correo;

    /**
     * Rol del usuario en el sistema
     */
    private String rol;

    /**
     * Fecha de creación del usuario
     */
    private LocalDateTime fechaCreacion;

    /**
     * Indica si el usuario está activo
     */
    private Boolean activo;
}
