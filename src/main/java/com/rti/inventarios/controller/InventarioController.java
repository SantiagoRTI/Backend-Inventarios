package com.rti.inventarios.controller;

import com.rti.inventarios.model.dto.*;
import com.rti.inventarios.model.entity.Activo;
import com.rti.inventarios.repository.ActivoRepository;
import com.rti.inventarios.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controlador REST para gestión de inventarios.
 * 
 * Proporciona endpoints para CRUD de inventarios, carga de Excel,
 * consulta de información cargada y ejecución de cruces.
 */
@RestController
@RequestMapping("/api/inventarios")
@Tag(name = "Inventarios", description = "Gestión de inventarios y procesamiento de activos")
@SecurityRequirement(name = "Bearer Authentication")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ExcelCargaService excelCargaService;

    @Autowired
    private CruceInventarioService cruceInventarioService;

    @Autowired
    private ReporteAuditoriaService reporteAuditoriaService;

    @Autowired
    private ActivoRepository activoRepository;

    /**
     * Obtiene todos los inventarios del sistema.
     * Solo accesible por administradores.
     * 
     * @return Lista de inventarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar inventarios", 
               description = "Obtiene todos los inventarios")
    public ResponseEntity<List<InventarioResponse>> obtenerTodos() {
        List<InventarioResponse> inventarios = inventarioService.obtenerTodos();
        return ResponseEntity.ok(inventarios);
    }

    /**
     * Obtiene un inventario específico por su ID.
     * 
     * @param id ID del inventario
     * @return Inventario encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener inventario", 
               description = "Obtiene un inventario por su ID")
    public ResponseEntity<InventarioResponse> obtenerPorId(@PathVariable Long id) {
        InventarioResponse inventario = inventarioService.obtenerPorId(id);
        return ResponseEntity.ok(inventario);
    }

    /**
     * Valida un inventario por su código (usado por inspectores).
     * 
     * @param codigo Código del inventario
     * @return Inventario si está asignado al inspector
     */
    @GetMapping("/validar/{codigo}")
    @PreAuthorize("hasRole('INSPECTOR')")
    @Operation(summary = "Validar inventario", 
               description = "Valida que un inventario existe y está asignado al inspector")
    public ResponseEntity<InventarioResponse> validarInventario(@PathVariable String codigo) {
        InventarioResponse inventario = inventarioService.validarPorCodigo(codigo);
        return ResponseEntity.ok(inventario);
    }

    /**
     * Crea un nuevo inventario.
     * Solo accesible por administradores.
     * 
     * @param request Datos del nuevo inventario
     * @return Inventario creado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear inventario", 
               description = "Crea un nuevo inventario")
    public ResponseEntity<InventarioResponse> crear(@Valid @RequestBody InventarioRequest request) {
        InventarioResponse inventario = inventarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventario);
    }

    /**
     * Actualiza un inventario existente.
     * Solo accesible por administradores.
     * 
     * @param id ID del inventario
     * @param request Nuevos datos del inventario
     * @return Inventario actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar inventario", 
               description = "Actualiza un inventario existente")
    public ResponseEntity<InventarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody InventarioRequest request) {
        InventarioResponse inventario = inventarioService.actualizar(id, request);
        return ResponseEntity.ok(inventario);
    }

    /**
     * Elimina un inventario.
     * Solo accesible por administradores.
     * 
     * @param id ID del inventario
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar inventario", 
               description = "Desactiva un inventario")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        inventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Descarga la plantilla Excel para carga de activos.
     * 
     * @return Archivo Excel de plantilla
     */
    @GetMapping("/{id}/plantilla")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Descargar plantilla Excel", 
               description = "Genera y descarga la plantilla Excel para carga de activos")
    public ResponseEntity<Resource> descargarPlantilla(@PathVariable Long id) throws IOException {
        Resource resource = excelCargaService.generarPlantilla();
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_activos.xlsx")
                .body(resource);
    }

    /**
     * Carga activos desde un archivo Excel (origen administrador).
     * 
     * @param id ID del inventario
     * @param archivo Archivo Excel con los activos
     * @return Resultado de la carga
     */
    @PostMapping("/{id}/cargar-excel")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cargar activos desde Excel", 
               description = "Procesa un archivo Excel y carga los activos del administrador")
    public ResponseEntity<CargaExcelResponse> cargarExcel(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) throws IOException {
        CargaExcelResponse response = excelCargaService.cargarExcel(id, archivo);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los activos cargados por el administrador.
     * 
     * @param id ID del inventario
     * @return Lista de activos del administrador
     */
    @GetMapping("/{id}/activos-administrador")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Obtener activos del administrador", 
               description = "Consulta los activos cargados por el administrador")
    public ResponseEntity<List<ActivoResponse>> obtenerActivosAdministrador(@PathVariable Long id) {
        List<Activo> activos = activoRepository.findByInventarioIdAndOrigen(
                id, com.rti.inventarios.model.enums.OrigenActivo.ADMINISTRADOR
        );
        
        List<ActivoResponse> response = activos.stream()
                .map(a -> ActivoResponse.builder()
                        .id(a.getId())
                        .inventarioId(a.getInventario().getId())
                        .idActivo(a.getIdActivo())
                        .etiqueta(a.getEtiqueta())
                        .descripcion(a.getDescripcion())
                        .marca(a.getMarca())
                        .serial(a.getSerial())
                        .modelo(a.getModelo())
                        .responsable(a.getResponsable())
                        .ciudad(a.getCiudad())
                        .estado(a.getEstado())
                        .origen(a.getOrigen().name())
                        .fechaCreacion(a.getFechaCreacion())
                        .build())
                .toList();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los activos registrados por el inspector.
     * 
     * @param id ID del inventario
     * @return Lista de activos del inspector
     */
    @GetMapping("/{id}/activos-inspector")
    @Operation(summary = "Obtener activos del inspector", 
               description = "Consulta los activos registrados por el inspector")
    public ResponseEntity<List<ActivoResponse>> obtenerActivosInspector(@PathVariable Long id) {
        List<Activo> activos = activoRepository.findByInventarioIdAndOrigen(
                id, com.rti.inventarios.model.enums.OrigenActivo.INSPECTOR
        );
        
        List<ActivoResponse> response = activos.stream()
                .map(a -> ActivoResponse.builder()
                        .id(a.getId())
                        .inventarioId(a.getInventario().getId())
                        .idActivo(a.getIdActivo())
                        .etiqueta(a.getEtiqueta())
                        .descripcion(a.getDescripcion())
                        .marca(a.getMarca())
                        .serial(a.getSerial())
                        .modelo(a.getModelo())
                        .responsable(a.getResponsable())
                        .ciudad(a.getCiudad())
                        .estado(a.getEstado())
                        .origen(a.getOrigen().name())
                        .fechaCreacion(a.getFechaCreacion())
                        .build())
                .toList();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Ejecuta el cruce de información entre activos del administrador e inspector.
     * 
     * @param id ID del inventario
     * @return Resultado del cruce
     */
    @PostMapping("/{id}/cruce")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INSPECTOR')")
    @Operation(summary = "Ejecutar cruce de inventario", 
               description = "Compara activos del administrador vs inspector y genera resultado")
    public ResponseEntity<CruceResponse> ejecutarCruce(@PathVariable Long id) {
        CruceResponse response = cruceInventarioService.ejecutarCruce(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el último resultado de cruce ejecutado.
     * 
     * @param id ID del inventario
     * @return Último resultado de cruce
     */
    @GetMapping("/{id}/activos-cruzados")
    @Operation(summary = "Obtener resultado del cruce", 
               description = "Consulta el último resultado del cruce")
    public ResponseEntity<CruceResponse> obtenerResultadoCruce(@PathVariable Long id) {
        CruceResponse response = cruceInventarioService.obtenerUltimoCruce(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Descarga el resultado del cruce en formato Excel.
     * Accesible tanto para administradores como para inspectores.
     * 
     * @param id ID del inventario
     * @return Archivo Excel con el resultado del cruce
     */
    @GetMapping("/{id}/cruce/excel")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INSPECTOR')")
    @Operation(summary = "Descargar Excel del cruce", 
               description = "Genera y descarga Excel con el resultado del cruce")
    public ResponseEntity<Resource> descargarExcelCruce(@PathVariable Long id) throws IOException {
        Resource resource = reporteAuditoriaService.generarExcelCruce(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cruce_inventario_" + id + ".xlsx")
                .body(resource);
    }

    /**
     * Descarga Excel solo con activos del administrador.
     * 
     * @param id ID del inventario
     * @return Archivo Excel con activos del administrador
     */
    @GetMapping("/{id}/activos-administrador/excel")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Descargar Excel activos administrador", 
               description = "Genera Excel solo con activos cargados por el administrador")
    public ResponseEntity<Resource> descargarExcelActivosAdministrador(@PathVariable Long id) throws IOException {
        Resource resource = reporteAuditoriaService.generarExcelActivosAdministrador(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activos_administrador_" + id + ".xlsx")
                .body(resource);
    }
}
