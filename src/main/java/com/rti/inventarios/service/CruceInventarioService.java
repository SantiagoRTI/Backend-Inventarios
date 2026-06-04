package com.rti.inventarios.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rti.inventarios.exception.BusinessException;
import com.rti.inventarios.exception.ResourceNotFoundException;
import com.rti.inventarios.model.dto.ActivoCruzadoResponse;
import com.rti.inventarios.model.dto.CruceResponse;
import com.rti.inventarios.model.entity.Activo;
import com.rti.inventarios.model.entity.DetalleCruce;
import com.rti.inventarios.model.entity.Inventario;
import com.rti.inventarios.model.entity.ResultadoCruce;
import com.rti.inventarios.model.enums.EstadoCruce;
import com.rti.inventarios.model.enums.OrigenActivo;
import com.rti.inventarios.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para ejecutar el cruce de información entre activos del administrador y del inspector.
 * 
 * Compara los activos cargados inicialmente por el administrador contra los registrados
 * por el inspector durante la inspección, clasificándolos en:
 * - CRUCE_NORMAL: Coinciden todos los campos
 * - EDITADO: Existen diferencias en algún campo
 * - SOBRANTE: Solo existe en carga del inspector
 * - FALTANTE: Solo existe en carga del administrador
 */
@Service
public class CruceInventarioService {

    private static final Logger logger = LoggerFactory.getLogger(CruceInventarioService.class);

    private static final String[] CAMPOS_COMPARABLES = {
            "etiqueta", "descripcion", "marca", "serial", "modelo", "responsable", "ciudad"
    };

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ActivoRepository activoRepository;

    @Autowired
    private ResultadoCruceRepository resultadoCruceRepository;

    @Autowired
    private DetalleCruceRepository detalleCruceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Ejecuta el proceso de cruce para un inventario específico
     * 
     * @param inventarioId ID del inventario
     * @return Resultado del cruce con estadísticas y detalles
     */
    @Transactional
    public CruceResponse ejecutarCruce(Long inventarioId) {
        logger.info("Iniciando cruce de inventario ID: {}", inventarioId);

        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario no encontrado con ID: " + inventarioId
                ));

        List<Activo> activosAdmin = activoRepository.findByInventarioIdAndOrigen(
                inventarioId, OrigenActivo.ADMINISTRADOR
        );

        List<Activo> activosInspector = activoRepository.findByInventarioIdAndOrigen(
                inventarioId, OrigenActivo.INSPECTOR
        );

        if (activosAdmin.isEmpty() && activosInspector.isEmpty()) {
            throw new BusinessException("No hay activos cargados para realizar el cruce");
        }

        logger.debug("Activos administrador: {}, Activos inspector: {}", 
                     activosAdmin.size(), activosInspector.size());

        Map<String, Activo> mapaAdmin = activosAdmin.stream()
                .collect(Collectors.toMap(Activo::getIdActivo, a -> a));

        Map<String, Activo> mapaInspector = activosInspector.stream()
                .collect(Collectors.toMap(Activo::getIdActivo, a -> a));

        List<DetalleCruce> detalles = new ArrayList<>();
        int cruceNormal = 0;
        int editados = 0;
        int sobrantes = 0;
        int faltantes = 0;

        Set<String> idsInspector = new HashSet<>(mapaInspector.keySet());

        for (String idActivo : idsInspector) {
            Activo activoInspector = mapaInspector.get(idActivo);
            Activo activoAdmin = mapaAdmin.get(idActivo);

            DetalleCruce detalle;

            if (activoAdmin == null) {
                detalle = crearDetalleSobrante(activoInspector);
                sobrantes++;
            } else {
                List<String> camposModificados = compararActivos(activoAdmin, activoInspector);
                
                if (camposModificados.isEmpty()) {
                    detalle = crearDetalleCruceNormal(activoInspector);
                    cruceNormal++;
                } else {
                    detalle = crearDetalleEditado(activoInspector, camposModificados);
                    editados++;
                }
            }

            detalles.add(detalle);
        }

        for (String idActivo : mapaAdmin.keySet()) {
            if (!idsInspector.contains(idActivo)) {
                Activo activoAdmin = mapaAdmin.get(idActivo);
                DetalleCruce detalle = crearDetalleFaltante(activoAdmin);
                detalles.add(detalle);
                faltantes++;
            }
        }

        ResultadoCruce resultado = ResultadoCruce.builder()
                .inventario(inventario)
                .fechaCruce(LocalDateTime.now())
                .totalActivos(detalles.size())
                .cruceNormal(cruceNormal)
                .editados(editados)
                .sobrantes(sobrantes)
                .faltantes(faltantes)
                .build();

        resultado = resultadoCruceRepository.save(resultado);

        for (DetalleCruce detalle : detalles) {
            detalle.setResultadoCruce(resultado);
        }
        detalleCruceRepository.saveAll(detalles);

        logger.info("Cruce completado. Total: {}, Normal: {}, Editados: {}, Sobrantes: {}, Faltantes: {}",
                    detalles.size(), cruceNormal, editados, sobrantes, faltantes);

        return construirRespuesta(resultado, detalles);
    }

    /**
     * Obtiene el último resultado de cruce para un inventario
     * 
     * @param inventarioId ID del inventario
     * @return Último resultado de cruce
     */
    @Transactional(readOnly = true)
    public CruceResponse obtenerUltimoCruce(Long inventarioId) {
        logger.debug("Obteniendo último cruce para inventario: {}", inventarioId);

        ResultadoCruce resultado = resultadoCruceRepository
                .findFirstByInventarioIdOrderByFechaCruceDesc(inventarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se ha ejecutado ningún cruce para el inventario: " + inventarioId
                ));

        List<DetalleCruce> detalles = detalleCruceRepository.findByResultadoCruceId(resultado.getId());

        return construirRespuesta(resultado, detalles);
    }

    /**
     * Compara dos activos campo por campo y devuelve la lista de campos modificados
     */
    private List<String> compararActivos(Activo admin, Activo inspector) {
        List<String> modificados = new ArrayList<>();

        if (!sonIguales(admin.getEtiqueta(), inspector.getEtiqueta())) {
            modificados.add("etiqueta");
        }
        if (!sonIguales(admin.getDescripcion(), inspector.getDescripcion())) {
            modificados.add("descripcion");
        }
        if (!sonIguales(admin.getMarca(), inspector.getMarca())) {
            modificados.add("marca");
        }
        if (!sonIguales(admin.getSerial(), inspector.getSerial())) {
            modificados.add("serial");
        }
        if (!sonIguales(admin.getModelo(), inspector.getModelo())) {
            modificados.add("modelo");
        }
        if (!sonIguales(admin.getResponsable(), inspector.getResponsable())) {
            modificados.add("responsable");
        }
        if (!sonIguales(admin.getCiudad(), inspector.getCiudad())) {
            modificados.add("ciudad");
        }

        return modificados;
    }

    /**
     * Compara dos strings con normalización (trim y case-insensitive)
     */
    private boolean sonIguales(String valor1, String valor2) {
        if (valor1 == null && valor2 == null) {
            return true;
        }
        if (valor1 == null || valor2 == null) {
            return false;
        }
        return valor1.trim().equalsIgnoreCase(valor2.trim());
    }

    /**
     * Crea un detalle de cruce para un activo con estado CRUCE_NORMAL
     */
    private DetalleCruce crearDetalleCruceNormal(Activo activo) {
        return DetalleCruce.builder()
                .idActivo(activo.getIdActivo())
                .etiqueta(activo.getEtiqueta())
                .descripcion(activo.getDescripcion())
                .marca(activo.getMarca())
                .serial(activo.getSerial())
                .modelo(activo.getModelo())
                .responsable(activo.getResponsable())
                .ciudad(activo.getCiudad())
                .estadoCruce(EstadoCruce.CRUCE_NORMAL)
                .camposModificados("[]")
                .build();
    }

    /**
     * Crea un detalle de cruce para un activo con estado EDITADO
     */
    private DetalleCruce crearDetalleEditado(Activo activo, List<String> camposModificados) {
        String camposJson;
        try {
            camposJson = objectMapper.writeValueAsString(camposModificados);
        } catch (JsonProcessingException e) {
            camposJson = "[]";
            logger.warn("Error al serializar campos modificados", e);
        }

        return DetalleCruce.builder()
                .idActivo(activo.getIdActivo())
                .etiqueta(activo.getEtiqueta())
                .descripcion(activo.getDescripcion())
                .marca(activo.getMarca())
                .serial(activo.getSerial())
                .modelo(activo.getModelo())
                .responsable(activo.getResponsable())
                .ciudad(activo.getCiudad())
                .estadoCruce(EstadoCruce.EDITADO)
                .camposModificados(camposJson)
                .build();
    }

    /**
     * Crea un detalle de cruce para un activo con estado SOBRANTE
     */
    private DetalleCruce crearDetalleSobrante(Activo activo) {
        return DetalleCruce.builder()
                .idActivo(activo.getIdActivo())
                .etiqueta(activo.getEtiqueta())
                .descripcion(activo.getDescripcion())
                .marca(activo.getMarca())
                .serial(activo.getSerial())
                .modelo(activo.getModelo())
                .responsable(activo.getResponsable())
                .ciudad(activo.getCiudad())
                .estadoCruce(EstadoCruce.SOBRANTE)
                .camposModificados("[]")
                .build();
    }

    /**
     * Crea un detalle de cruce para un activo con estado FALTANTE
     */
    private DetalleCruce crearDetalleFaltante(Activo activo) {
        return DetalleCruce.builder()
                .idActivo(activo.getIdActivo())
                .etiqueta(activo.getEtiqueta())
                .descripcion(activo.getDescripcion())
                .marca(activo.getMarca())
                .serial(activo.getSerial())
                .modelo(activo.getModelo())
                .responsable(activo.getResponsable())
                .ciudad(activo.getCiudad())
                .estadoCruce(EstadoCruce.FALTANTE)
                .camposModificados("[]")
                .build();
    }

    /**
     * Construye la respuesta del cruce a partir del resultado y detalles
     */
    private CruceResponse construirRespuesta(ResultadoCruce resultado, List<DetalleCruce> detalles) {
        List<ActivoCruzadoResponse> activos = detalles.stream()
                .map(this::convertirDetalleAResponse)
                .collect(Collectors.toList());

        CruceResponse.ResumenCruce resumen = CruceResponse.ResumenCruce.builder()
                .total(resultado.getTotalActivos())
                .cruceNormal(resultado.getCruceNormal())
                .editado(resultado.getEditados())
                .sobrante(resultado.getSobrantes())
                .faltante(resultado.getFaltantes())
                .build();

        return CruceResponse.builder()
                .inventarioId(resultado.getInventario().getId())
                .fechaCruce(resultado.getFechaCruce())
                .resumen(resumen)
                .activos(activos)
                .build();
    }

    /**
     * Convierte un DetalleCruce a ActivoCruzadoResponse
     */
    private ActivoCruzadoResponse convertirDetalleAResponse(DetalleCruce detalle) {
        List<String> camposModificados = new ArrayList<>();
        
        if (detalle.getCamposModificados() != null && !detalle.getCamposModificados().equals("[]")) {
            try {
                camposModificados = objectMapper.readValue(
                        detalle.getCamposModificados(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
            } catch (JsonProcessingException e) {
                logger.warn("Error al deserializar campos modificados", e);
            }
        }

        String estadoStr = detalle.getEstadoCruce().name();
        if (estadoStr.equals("CRUCE_NORMAL")) {
            estadoStr = "CRUCE NORMAL";
        }

        return ActivoCruzadoResponse.builder()
                .idActivo(detalle.getIdActivo())
                .etiqueta(detalle.getEtiqueta())
                .descripcion(detalle.getDescripcion())
                .marca(detalle.getMarca())
                .serial(detalle.getSerial())
                .modelo(detalle.getModelo())
                .responsable(detalle.getResponsable())
                .ciudad(detalle.getCiudad())
                .estado(estadoStr)
                .camposModificados(camposModificados)
                .build();
    }
}
