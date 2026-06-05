package com.rti.inventarios.controller;

import com.rti.inventarios.model.dto.ActivoRequest;
import com.rti.inventarios.model.dto.ActivoResponse;
import com.rti.inventarios.service.ActivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de activos por inspectores.
 * 
 * Proporciona endpoints para consultar, registrar y actualizar activos
 * durante el proceso de inspección.
 */
@RestController
@RequestMapping("/api/activos")
@Tag(name = "Activos", description = "Gestión de activos por inspectores")
@SecurityRequirement(name = "Bearer Authentication")
public class ActivoController {

    @Autowired
    private ActivoService activoService;

    /**
     * Busca un activo por su código/ID de activo cargado por el administrador.
     * Retorna activos que fueron subidos desde el Excel por el administrador
     * para que el inspector pueda consultarlos durante la inspección.
     * 
     * @param codigo Código del activo
     * @return Activo encontrado
     */
    @GetMapping("/{codigo}")
    @Operation(summary = "Buscar activo por código", 
               description = "Busca un activo cargado por el administrador usando su código")
    public ResponseEntity<ActivoResponse> buscarPorCodigo(@PathVariable String codigo) {
        ActivoResponse activo = activoService.buscarPorIdActivo(codigo);
        return ResponseEntity.ok(activo);
    }

    /**
     * Busca un activo por su código de barras (etiqueta) cargado por el administrador.
     * Retorna activos que fueron subidos desde el Excel por el administrador
     * para que el inspector pueda consultarlos durante la inspección.
     * 
     * @param barcode Código de barras del activo
     * @return Activo encontrado
     */
    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Buscar activo por barcode", 
               description = "Busca un activo cargado por el administrador por su código de barras o etiqueta")
    public ResponseEntity<ActivoResponse> buscarPorBarcode(@PathVariable String barcode) {
        ActivoResponse activo = activoService.buscarPorBarcode(barcode);
        return ResponseEntity.ok(activo);
    }

    /**
     * Registra un nuevo activo o actualiza uno existente durante la inspección.
     * Si el activo ya existe, actualiza sus datos.
     * 
     * @param request Datos del activo a registrar
     * @return Activo registrado o actualizado con código 201 (Created)
     */
    @PostMapping
    @Operation(summary = "Registrar activo", 
               description = "Registra un nuevo activo o actualiza uno existente durante la inspección")
    public ResponseEntity<ActivoResponse> registrar(@Valid @RequestBody ActivoRequest request) {
        ActivoResponse activo = activoService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(activo);
    }

    /**
     * Actualiza un activo existente.
     * Solo se pueden actualizar activos registrados por inspectores.
     * 
     * @param id ID del registro del activo
     * @param request Nuevos datos del activo
     * @return Activo actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar activo", 
               description = "Actualiza un activo registrado por el inspector")
    public ResponseEntity<ActivoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActivoRequest request) {
        ActivoResponse activo = activoService.actualizar(id, request);
        return ResponseEntity.ok(activo);
    }
}
