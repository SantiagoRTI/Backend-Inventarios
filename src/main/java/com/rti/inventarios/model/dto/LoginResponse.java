package com.rti.inventarios.model.dto;

import lombok.*;

/**
 * DTO para la respuesta de autenticación exitosa.
 * 
 * Contiene el token JWT generado y la información básica del usuario autenticado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * Token JWT para autenticación en peticiones subsiguientes
     */
    private String token;

    /**
     * Correo electrónico del usuario autenticado
     */
    private String usuario;

    /**
     * Rol del usuario en el sistema
     */
    private String rol;
}
