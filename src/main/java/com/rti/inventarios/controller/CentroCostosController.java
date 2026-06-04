package com.rti.inventarios.controller;

import com.rti.inventarios.model.dto.CentroCostosResponse;
import com.rti.inventarios.service.CentroCostosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para consulta de centros de costos.
 * 
 * Proporciona endpoints para obtener información de centros de costos
 * o ubicaciones físicas donde se realizan inventarios.
 */
@RestController
@RequestMapping("/api/centros")
@Tag(name = "Centros de Costos", description = "Consulta de centros de costos")
@SecurityRequirement(name = "Bearer Authentication")
public class CentroCostosController {

    @Autowired
    private CentroCostosService centroCostosService;

    /**
     * Obtiene un centro de costos por su código.
     * 
     * @param codigo Código del centro de costos
     * @return Centro de costos encontrado
     */
    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener centro de costos", 
               description = "Busca un centro de costos por su código")
    public ResponseEntity<CentroCostosResponse> obtenerPorCodigo(@PathVariable String codigo) {
        CentroCostosResponse centroCostos = centroCostosService.obtenerPorCodigo(codigo);
        return ResponseEntity.ok(centroCostos);
    }
}
