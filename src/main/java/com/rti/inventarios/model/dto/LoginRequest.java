package com.rti.inventarios.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO para la solicitud de autenticación (login).
 * 
 * Contiene las credenciales del usuario para iniciar sesión en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * Correo electrónico del usuario (usado como username)
     */
    @NotBlank(message = "El usuario es obligatorio")
    @Email(message = "Debe proporcionar un correo electrónico válido")
    private String usuario;

    /**
     * Contraseña del usuario
     */
    @NotBlank(message = "La contraseña es obligatoria")
    private String contraseña;
}
