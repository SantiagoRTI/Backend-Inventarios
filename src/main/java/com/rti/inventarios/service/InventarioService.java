package com.rti.inventarios.service;

import com.rti.inventarios.exception.BusinessException;
import com.rti.inventarios.exception.ResourceNotFoundException;
import com.rti.inventarios.model.dto.*;
import com.rti.inventarios.model.entity.CentroCostos;
import com.rti.inventarios.model.entity.Inventario;
import com.rti.inventarios.model.entity.Usuario;
import com.rti.inventarios.model.enums.RolUsuario;
import com.rti.inventarios.repository.CentroCostosRepository;
import com.rti.inventarios.repository.InventarioRepository;
import com.rti.inventarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de inventarios.
 * 
 * Maneja la creación, consulta, actualización y asignación de inventarios a inspectores.
 */
@Service
public class InventarioService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CentroCostosRepository centroCostosRepository;

    /**
     * Obtiene todos los inventarios del sistema
     * 
     * @return Lista de inventarios
     */
    @Transactional(readOnly = true)
    public List<InventarioResponse> obtenerTodos() {
        logger.debug("Obteniendo todos los inventarios");
        return inventarioRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un inventario por su ID
     * 
     * @param id ID del inventario
     * @return Inventario encontrado
     */
    @Transactional(readOnly = true)
    public InventarioResponse obtenerPorId(Long id) {
        logger.debug("Obteniendo inventario con ID: {}", id);
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado con ID: " + id));
        return convertirAResponse(inventario);
    }

    /**
     * Valida un inventario por su código (usado por el inspector)
     * 
     * @param codigo Código del inventario
     * @return Inventario encontrado
     */
    @Transactional(readOnly = true)
    public InventarioResponse validarPorCodigo(String codigo) {
        logger.debug("Validando inventario con código: {}", codigo);
        Inventario inventario = inventarioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado con código: " + codigo));
        return convertirAResponse(inventario);
    }

    /**
     * Crea un nuevo inventario
     * 
     * @param request Datos del inventario a crear
     * @return Inventario creado
     */
    @Transactional
    public InventarioResponse crear(InventarioRequest request) {
        logger.info("Creando nuevo inventario con código: {}", request.getCodigo());

        if (inventarioRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe un inventario con el código: " + request.getCodigo());
        }

        Inventario inventario = Inventario.builder()
                .nombre(request.getNombre())
                .codigo(request.getCodigo())
                .estado(request.getEstado() != null ? request.getEstado() : "Ejecucion")
                .activo(true)
                .build();

        if (request.getInspectorId() != null) {
            Usuario inspector = usuarioRepository.findById(request.getInspectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspector no encontrado con ID: " + request.getInspectorId()));
            
            if (!inspector.getRol().equals(RolUsuario.INSPECTOR)) {
                throw new BusinessException("El usuario asignado debe tener rol de Inspector");
            }
            inventario.setInspector(inspector);
        }

        inventario = inventarioRepository.save(inventario);
        logger.info("Inventario creado exitosamente con ID: {}", inventario.getId());

        return convertirAResponse(inventario);
    }

    /**
     * Actualiza un inventario existente
     * 
     * @param id ID del inventario a actualizar
     * @param request Nuevos datos del inventario
     * @return Inventario actualizado
     */
    @Transactional
    public InventarioResponse actualizar(Long id, InventarioRequest request) {
        logger.info("Actualizando inventario con ID: {}", id);

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado con ID: " + id));

        if (!inventario.getCodigo().equals(request.getCodigo()) &&
                inventarioRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe otro inventario con el código: " + request.getCodigo());
        }

        inventario.setNombre(request.getNombre());
        inventario.setCodigo(request.getCodigo());
        if (request.getEstado() != null) {
            inventario.setEstado(request.getEstado());
        }

        if (request.getInspectorId() != null) {
            Usuario inspector = usuarioRepository.findById(request.getInspectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspector no encontrado con ID: " + request.getInspectorId()));
            
            if (!inspector.getRol().equals(RolUsuario.INSPECTOR)) {
                throw new BusinessException("El usuario asignado debe tener rol de Inspector");
            }
            inventario.setInspector(inspector);
        }

        inventario = inventarioRepository.save(inventario);
        logger.info("Inventario actualizado exitosamente");

        return convertirAResponse(inventario);
    }

    /**
     * Elimina un inventario (desactivación lógica)
     * 
     * @param id ID del inventario a eliminar
     */
    @Transactional
    public void eliminar(Long id) {
        logger.info("Eliminando inventario con ID: {}", id);

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado con ID: " + id));

        inventario.setActivo(false);
        inventarioRepository.save(inventario);

        logger.info("Inventario desactivado exitosamente");
    }

    /**
     * Convierte una entidad Inventario a InventarioResponse
     */
    private InventarioResponse convertirAResponse(Inventario inventario) {
        InventarioResponse response = InventarioResponse.builder()
                .id(inventario.getId())
                .nombre(inventario.getNombre())
                .codigo(inventario.getCodigo())
                .estado(inventario.getEstado())
                .fechaCreacion(inventario.getFechaCreacion())
                .fechaFinalizacion(inventario.getFechaFinalizacion())
                .activo(inventario.getActivo())
                .build();

        if (inventario.getInspector() != null) {
            Usuario inspector = inventario.getInspector();
            response.setInspector(UsuarioResponse.builder()
                    .id(inspector.getId())
                    .nombre(inspector.getNombre())
                    .correo(inspector.getCorreo())
                    .rol("Inspector")
                    .build());
        }

        if (inventario.getCentroCostos() != null) {
            CentroCostos cc = inventario.getCentroCostos();
            response.setCentroCostos(CentroCostosResponse.builder()
                    .id(cc.getId())
                    .codigo(cc.getCodigo())
                    .nombre(cc.getNombre())
                    .ciudad(cc.getCiudad())
                    .direccion(cc.getDireccion())
                    .build());
        }

        return response;
    }
}
