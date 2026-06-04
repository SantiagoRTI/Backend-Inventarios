package com.rti.inventarios.controller;

import com.rti.inventarios.model.dto.CambioContrasenaRequest;
import com.rti.inventarios.model.dto.UsuarioRequest;
import com.rti.inventarios.model.dto.UsuarioResponse;
import com.rti.inventarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de usuarios.
 * 
 * Proporciona endpoints CRUD para usuarios del sistema.
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Obtiene la lista de todos los usuarios del sistema.
     * Solo accesible por administradores.
     * 
     * @return Lista de usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Listar usuarios", 
                description = "Obtiene todos los usuarios del sistema (solo administradores)")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodos() {
        List<UsuarioResponse> usuarios = usuarioService.obtenerTodos();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene un usuario específico por su ID.
     * Solo accesible por administradores.
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener usuario", 
                description = "Obtiene un usuario por su ID")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * Acceso público para registro de usuarios.
     * 
     * @param request Datos del nuevo usuario
     * @return Usuario creado con código 201 (Created)
     */
    @PostMapping
    @Operation(summary = "Crear usuario", 
                description = "Crea un nuevo usuario en el sistema")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    /**
     * Actualiza un usuario existente.
     * Solo accesible por administradores.
     * 
     * @param id ID del usuario a actualizar
     * @param request Nuevos datos del usuario
     * @return Usuario actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Actualizar usuario", 
                description = "Actualiza los datos de un usuario existente")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Cambia la contraseña de un usuario.
     * Requiere la contraseña actual para validación.
     * 
     * @param id ID del usuario
     * @param request Contraseña actual y nueva contraseña
     * @return Respuesta sin contenido (204)
     */
    @PutMapping("/{id}/cambiar-contrasena")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cambiar contraseña", 
                description = "Cambia la contraseña de un usuario (requiere contraseña actual)")
    public ResponseEntity<Void> cambiarContrasena(
            @PathVariable Long id,
            @Valid @RequestBody CambioContrasenaRequest request) {
        usuarioService.cambiarContrasena(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina (desactiva) un usuario del sistema.
     * Solo accesible por administradores.
     * 
     * @param id ID del usuario a eliminar
     * @return Respuesta sin contenido (204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar usuario", 
                description = "Desactiva un usuario del sistema")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
