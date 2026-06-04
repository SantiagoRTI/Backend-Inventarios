package com.rti.inventarios.service;

import com.rti.inventarios.config.JwtUtil;
import com.rti.inventarios.model.dto.LoginRequest;
import com.rti.inventarios.model.dto.LoginResponse;
import com.rti.inventarios.model.entity.Usuario;
import com.rti.inventarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación que maneja el proceso de login de usuarios.
 * 
 * Valida las credenciales del usuario y genera tokens JWT para acceso a la API.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Autentica un usuario y genera un token JWT
     * 
     * @param loginRequest Credenciales de login
     * @return LoginResponse con el token JWT y datos del usuario
     */
    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Intento de login para usuario: {}", loginRequest.getUsuario());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsuario(),
                        loginRequest.getContraseña()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        Usuario usuario = usuarioRepository.findByCorreo(loginRequest.getUsuario())
                .orElseThrow();

        String rol = usuario.getRol().name();
        if (rol.equals("ADMINISTRADOR")) {
            rol = "Administrador";
        } else if (rol.equals("INSPECTOR")) {
            rol = "Inspector";
        }

        logger.info("Login exitoso para usuario: {} con rol: {}", usuario.getCorreo(), rol);

        return LoginResponse.builder()
                .token(token)
                .usuario(usuario.getCorreo())
                .rol(rol)
                .build();
    }
}
