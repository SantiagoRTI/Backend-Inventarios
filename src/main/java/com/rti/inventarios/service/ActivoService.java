package com.rti.inventarios.service;

import com.rti.inventarios.exception.BusinessException;
import com.rti.inventarios.exception.ResourceNotFoundException;
import com.rti.inventarios.model.dto.ActivoRequest;
import com.rti.inventarios.model.dto.ActivoResponse;
import com.rti.inventarios.model.entity.Activo;
import com.rti.inventarios.model.entity.Inventario;
import com.rti.inventarios.model.enums.OrigenActivo;
import com.rti.inventarios.repository.ActivoRepository;
import com.rti.inventarios.repository.InventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio para la gestión de activos registrados por inspectores.
 * 
 * Maneja el registro, consulta y actualización de activos durante el proceso de inspección.
 */
@Service
public class ActivoService {

    private static final Logger logger = LoggerFactory.getLogger(ActivoService.class);

    @Autowired
    private ActivoRepository activoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    /**
     * Busca un activo por su ID de activo cargado por el administrador.
     * Este servicio retorna activos que fueron subidos desde el Excel por el administrador.
     * 
     * @param idActivo Código del activo
     * @return Activo encontrado
     */
    @Transactional(readOnly = true)
    public ActivoResponse buscarPorIdActivo(String idActivo) {
        logger.debug("Buscando activo con ID: {}", idActivo);
        
        Optional<Activo> activoOpt = activoRepository.findAll().stream()
                .filter(a -> a.getIdActivo().equals(idActivo) && 
                             a.getOrigen().equals(OrigenActivo.ADMINISTRADOR))
                .findFirst();
        
        if (activoOpt.isEmpty()) {
            throw new ResourceNotFoundException("Activo no encontrado con ID: " + idActivo);
        }
        
        return convertirAResponse(activoOpt.get());
    }

    /**
     * Busca un activo por su código de barras (etiqueta) cargado por el administrador.
     * Este servicio retorna activos que fueron subidos desde el Excel por el administrador.
     * 
     * @param barcode Código de barras del activo
     * @return Activo encontrado
     */
    @Transactional(readOnly = true)
    public ActivoResponse buscarPorBarcode(String barcode) {
        logger.debug("Buscando activo con barcode: {}", barcode);
        
        Optional<Activo> activoOpt = activoRepository.findAll().stream()
                .filter(a -> barcode.equals(a.getEtiqueta()) && 
                             a.getOrigen().equals(OrigenActivo.ADMINISTRADOR))
                .findFirst();
        
        if (activoOpt.isEmpty()) {
            throw new ResourceNotFoundException("Activo no encontrado con barcode: " + barcode);
        }
        
        return convertirAResponse(activoOpt.get());
    }

    /**
     * Registra un nuevo activo o actualiza uno existente (origen inspector).
     * Si el activo ya existe, actualiza sus datos en lugar de crear uno nuevo.
     * 
     * @param request Datos del activo a registrar
     * @return Activo registrado o actualizado
     */
    @Transactional
    public ActivoResponse registrar(ActivoRequest request) {
        logger.info("Registrando activo con ID: {} en inventario: {}", 
                    request.getIdActivo(), request.getInventarioId());

        Inventario inventario = inventarioRepository.findById(request.getInventarioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario no encontrado con ID: " + request.getInventarioId()
                ));

        Optional<Activo> existente = activoRepository.findByInventarioIdAndIdActivoAndOrigen(
                request.getInventarioId(),
                request.getIdActivo(),
                OrigenActivo.INSPECTOR
        );

        Activo activo;
        
        if (existente.isPresent()) {
            // Actualizar el activo existente
            activo = existente.get();
            activo.setEtiqueta(request.getEtiqueta());
            activo.setDescripcion(request.getDescripcion());
            activo.setMarca(request.getMarca());
            activo.setSerial(request.getSerial());
            activo.setModelo(request.getModelo());
            activo.setResponsable(request.getResponsable());
            activo.setCiudad(request.getCiudad());
            activo.setEstado(request.getEstado());
            logger.info("Actualizando activo existente con ID: {}", activo.getIdActivo());
        } else {
            // Crear nuevo activo
            activo = Activo.builder()
                    .inventario(inventario)
                    .idActivo(request.getIdActivo())
                    .etiqueta(request.getEtiqueta())
                    .descripcion(request.getDescripcion())
                    .marca(request.getMarca())
                    .serial(request.getSerial())
                    .modelo(request.getModelo())
                    .responsable(request.getResponsable())
                    .ciudad(request.getCiudad())
                    .estado(request.getEstado())
                    .origen(OrigenActivo.INSPECTOR)
                    .build();
            logger.info("Creando nuevo activo con ID: {}", activo.getIdActivo());
        }

        activo = activoRepository.save(activo);
        logger.info("Activo guardado exitosamente con ID interno: {}", activo.getId());

        return convertirAResponse(activo);
    }

    /**
     * Actualiza un activo existente (origen inspector)
     * 
     * @param id ID del registro del activo
     * @param request Nuevos datos del activo
     * @return Activo actualizado
     */
    @Transactional
    public ActivoResponse actualizar(Long id, ActivoRequest request) {
        logger.info("Actualizando activo con ID: {}", id);

        Activo activo = activoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con ID: " + id));

        if (!activo.getOrigen().equals(OrigenActivo.INSPECTOR)) {
            throw new BusinessException("Solo se pueden actualizar activos registrados por inspectores");
        }

        activo.setIdActivo(request.getIdActivo());
        activo.setEtiqueta(request.getEtiqueta());
        activo.setDescripcion(request.getDescripcion());
        activo.setMarca(request.getMarca());
        activo.setSerial(request.getSerial());
        activo.setModelo(request.getModelo());
        activo.setResponsable(request.getResponsable());
        activo.setCiudad(request.getCiudad());
        activo.setEstado(request.getEstado());

        activo = activoRepository.save(activo);
        logger.info("Activo actualizado exitosamente");

        return convertirAResponse(activo);
    }

    /**
     * Convierte una entidad Activo a ActivoResponse
     */
    private ActivoResponse convertirAResponse(Activo activo) {
        return ActivoResponse.builder()
                .id(activo.getId())
                .inventarioId(activo.getInventario().getId())
                .idActivo(activo.getIdActivo())
                .etiqueta(activo.getEtiqueta())
                .descripcion(activo.getDescripcion())
                .marca(activo.getMarca())
                .serial(activo.getSerial())
                .modelo(activo.getModelo())
                .responsable(activo.getResponsable())
                .ciudad(activo.getCiudad())
                .estado(activo.getEstado())
                .origen(activo.getOrigen().name())
                .fechaCreacion(activo.getFechaCreacion())
                .build();
    }
}
