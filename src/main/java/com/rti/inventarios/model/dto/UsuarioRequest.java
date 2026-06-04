package com.rti.inventarios.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * DTO para la creación y actualización de usuarios.
 * 
 * Contiene la información necesaria para registrar o modificar un usuario en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequest {

    /**
     * Nombre completo del usuario
     */
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    /**
     * Correo electrónico del usuario (único en el sistema)
     */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe proporcionar un correo electrónico válido")
    private String correo;

    /**
     * Contraseña del usuario (solo requerido en creación)
     */
    private String contraseña;

    /**
     * Rol del usuario (Administrador o Inspector)
     */
    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(Administrador|Inspector)$", 
             message = "El rol debe ser 'Administrador' o 'Inspector'")
    private String rol;
}
