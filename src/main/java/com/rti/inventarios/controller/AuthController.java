package com.rti.inventarios.controller;

import com.rti.inventarios.model.dto.LoginRequest;
import com.rti.inventarios.model.dto.LoginResponse;
import com.rti.inventarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación de usuarios.
 * 
 * Proporciona endpoint público para login y generación de tokens JWT.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint de login para autenticación de usuarios.
     * Valida credenciales y genera un token JWT para acceso a la API.
     * 
     * @param loginRequest Credenciales del usuario (correo y contraseña)
     * @return LoginResponse con token JWT y datos del usuario autenticado
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", 
               description = "Autentica un usuario y devuelve un token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
