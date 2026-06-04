package com.rti.inventarios.service;

import com.rti.inventarios.exception.ResourceNotFoundException;
import com.rti.inventarios.model.dto.CentroCostosResponse;
import com.rti.inventarios.model.entity.CentroCostos;
import com.rti.inventarios.repository.CentroCostosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión de centros de costos.
 * 
 * Proporciona operaciones de consulta de centros de costos por código.
 */
@Service
public class CentroCostosService {

    private static final Logger logger = LoggerFactory.getLogger(CentroCostosService.class);

    @Autowired
    private CentroCostosRepository centroCostosRepository;

    /**
     * Obtiene un centro de costos por su código
     * 
     * @param codigo Código del centro de costos
     * @return Centro de costos encontrado
     */
    @Transactional(readOnly = true)
    public CentroCostosResponse obtenerPorCodigo(String codigo) {
        logger.debug("Obteniendo centro de costos con código: {}", codigo);
        
        CentroCostos centroCostos = centroCostosRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Centro de costos no encontrado con código: " + codigo
                ));
        
        return convertirAResponse(centroCostos);
    }

    /**
     * Convierte una entidad CentroCostos a CentroCostosResponse
     */
    private CentroCostosResponse convertirAResponse(CentroCostos centroCostos) {
        return CentroCostosResponse.builder()
                .id(centroCostos.getId())
                .codigo(centroCostos.getCodigo())
                .nombre(centroCostos.getNombre())
                .ciudad(centroCostos.getCiudad())
                .direccion(centroCostos.getDireccion())
                .activo(centroCostos.getActivo())
                .build();
    }
}
