package com.rti.inventarios.repository;

import com.rti.inventarios.model.entity.Usuario;
import com.rti.inventarios.model.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de usuarios.
 * 
 * Proporciona métodos CRUD estándar y consultas personalizadas para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico
     * 
     * @param correo Correo del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Verifica si existe un usuario con el correo dado
     * 
     * @param correo Correo a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCorreo(String correo);

    /**
     * Busca usuarios por rol
     * 
     * @param rol Rol a filtrar
     * @return Lista de usuarios con ese rol
     */
    List<Usuario> findByRol(RolUsuario rol);

    /**
     * Busca usuarios activos
     * 
     * @param activo Estado de activación
     * @return Lista de usuarios activos o inactivos
     */
    List<Usuario> findByActivo(Boolean activo);
}
