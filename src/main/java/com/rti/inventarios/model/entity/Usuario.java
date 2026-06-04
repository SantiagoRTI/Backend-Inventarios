package com.rti.inventarios.model.entity;

import com.rti.inventarios.model.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa un usuario del sistema con capacidades de autenticación.
 * Implementa UserDetails de Spring Security para integración con el framework de seguridad.
 * 
 * Los usuarios pueden tener rol de ADMINISTRADOR (gestión completa) o INSPECTOR
 * (registro de activos en campo).
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    /**
     * Identificador único del usuario
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Correo electrónico utilizado como login (único en el sistema)
     */
    @Column(nullable = false, unique = true)
    private String correo;

    /**
     * Contraseña encriptada con BCrypt
     */
    @Column(nullable = false)
    private String contrasena;

    /**
     * Rol del usuario en el sistema
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    /**
     * Fecha y hora de creación del usuario
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de última actualización del usuario
     */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Indica si el usuario está activo en el sistema
     */
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Inicializa las fechas de auditoría antes de persistir
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de modificación antes de actualizar
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Implementación de UserDetails para Spring Security

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
