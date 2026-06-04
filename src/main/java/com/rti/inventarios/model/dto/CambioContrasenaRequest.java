package com.rti.inventarios.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para la solicitud de cambio de contraseña de usuario.
 * 
 * Contiene la contraseña actual para validación y la nueva contraseña.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioContrasenaRequest {

    /**
     * Contraseña actual del usuario (para verificación)
     */
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String contrasenaActual;

    /**
     * Nueva contraseña del usuario
     */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasenaNueva;
}
