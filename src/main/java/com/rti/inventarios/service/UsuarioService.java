package com.rti.inventarios.service;

import com.rti.inventarios.exception.BusinessException;
import com.rti.inventarios.exception.ResourceNotFoundException;
import com.rti.inventarios.model.dto.CambioContrasenaRequest;
import com.rti.inventarios.model.dto.UsuarioRequest;
import com.rti.inventarios.model.dto.UsuarioResponse;
import com.rti.inventarios.model.entity.Usuario;
import com.rti.inventarios.model.enums.RolUsuario;
import com.rti.inventarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios del sistema.
 * 
 * Proporciona operaciones CRUD para usuarios con roles Administrador e Inspector.
 */
@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios del sistema
     * 
     * @return Lista de usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodos() {
        logger.debug("Obteniendo todos los usuarios");
        return usuarioRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por su ID
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        logger.debug("Obteniendo usuario con ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return convertirAResponse(usuario);
    }

    /**
     * Crea un nuevo usuario en el sistema
     * 
     * @param request Datos del usuario a crear
     * @return Usuario creado
     */
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        logger.info("Creando nuevo usuario con correo: {}", request.getCorreo());

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BusinessException("Ya existe un usuario con el correo: " + request.getCorreo());
        }

        if (request.getContraseña() == null || request.getContraseña().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria para crear un usuario");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .contrasena(passwordEncoder.encode(request.getContraseña()))
                .rol(convertirRol(request.getRol()))
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        logger.info("Usuario creado exitosamente con ID: {}", usuario.getId());

        return convertirAResponse(usuario);
    }

    /**
     * Actualiza un usuario existente
     * 
     * @param id ID del usuario a actualizar
     * @param request Nuevos datos del usuario
     * @return Usuario actualizado
     */
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        logger.info("Actualizando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (!usuario.getCorreo().equals(request.getCorreo()) &&
                usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BusinessException("Ya existe otro usuario con el correo: " + request.getCorreo());
        }

        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setRol(convertirRol(request.getRol()));

        if (request.getContraseña() != null && !request.getContraseña().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(request.getContraseña()));
        }

        usuario = usuarioRepository.save(usuario);
        logger.info("Usuario actualizado exitosamente");

        return convertirAResponse(usuario);
    }

    /**
     * Elimina un usuario del sistema (desactivación lógica)
     * 
     * @param id ID del usuario a eliminar
     */
    @Transactional
    public void eliminar(Long id) {
        logger.info("Eliminando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        logger.info("Usuario desactivado exitosamente");
    }

    /**
     * Cambia la contraseña de un usuario
     * 
     * @param id ID del usuario
     * @param request Contraseña actual y nueva contraseña
     */
    @Transactional
    public void cambiarContrasena(Long id, CambioContrasenaRequest request) {
        logger.info("Cambiando contraseña para usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new BusinessException("La contraseña actual es incorrecta");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getContrasenaNueva()));
        usuarioRepository.save(usuario);

        logger.info("Contraseña cambiada exitosamente");
    }

    /**
     * Convierte una cadena de rol a enum RolUsuario
     */
    private RolUsuario convertirRol(String rol) {
        if ("Administrador".equalsIgnoreCase(rol)) {
            return RolUsuario.ADMINISTRADOR;
        } else if ("Inspector".equalsIgnoreCase(rol)) {
            return RolUsuario.INSPECTOR;
        }
        throw new BusinessException("Rol inválido: " + rol);
    }

    /**
     * Convierte una entidad Usuario a UsuarioResponse
     */
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        String rolStr = usuario.getRol().name();
        if (rolStr.equals("ADMINISTRADOR")) {
            rolStr = "Administrador";
        } else if (rolStr.equals("INSPECTOR")) {
            rolStr = "Inspector";
        }

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(rolStr)
                .fechaCreacion(usuario.getFechaCreacion())
                .activo(usuario.getActivo())
                .build();
    }
}
