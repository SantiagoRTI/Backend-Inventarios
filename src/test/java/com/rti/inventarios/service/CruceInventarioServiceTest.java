package com.rti.inventarios.service;

import com.rti.inventarios.model.dto.CruceResponse;
import com.rti.inventarios.model.entity.Activo;
import com.rti.inventarios.model.entity.Inventario;
import com.rti.inventarios.model.entity.ResultadoCruce;
import com.rti.inventarios.model.enums.OrigenActivo;
import com.rti.inventarios.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CruceInventarioService.
 * 
 * Valida la lógica de comparación de activos y clasificación en estados:
 * CRUCE_NORMAL, EDITADO, SOBRANTE y FALTANTE.
 */
@ExtendWith(MockitoExtension.class)
class CruceInventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ActivoRepository activoRepository;

    @Mock
    private ResultadoCruceRepository resultadoCruceRepository;

    @Mock
    private DetalleCruceRepository detalleCruceRepository;

    @InjectMocks
    private CruceInventarioService cruceInventarioService;

    private Inventario inventario;

    @BeforeEach
    void setUp() {
        inventario = Inventario.builder()
                .id(1L)
                .codigo("INV-001")
                .nombre("Inventario Test")
                .build();
    }

    @Test
    @DisplayName("Debe clasificar como CRUCE_NORMAL cuando todos los campos coinciden")
    void debeClasificarComoCruceNormal() {
        Activo activoAdmin = crearActivo(1L, "ACT001", "Lenovo", "ABC123", OrigenActivo.ADMINISTRADOR);
        Activo activoInspector = crearActivo(2L, "ACT001", "Lenovo", "ABC123", OrigenActivo.INSPECTOR);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.ADMINISTRADOR))
                .thenReturn(List.of(activoAdmin));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.INSPECTOR))
                .thenReturn(List.of(activoInspector));
        when(resultadoCruceRepository.save(any())).thenAnswer(i -> {
            ResultadoCruce rc = i.getArgument(0);
            rc.setId(1L);
            return rc;
        });
        when(detalleCruceRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        CruceResponse response = cruceInventarioService.ejecutarCruce(1L);

        assertNotNull(response);
        assertEquals(1, response.getResumen().getTotal());
        assertEquals(1, response.getResumen().getCruceNormal());
        assertEquals(0, response.getResumen().getEditado());
        assertEquals(0, response.getResumen().getSobrante());
        assertEquals(0, response.getResumen().getFaltante());
        
        verify(resultadoCruceRepository, times(1)).save(any());
        verify(detalleCruceRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("Debe clasificar como EDITADO cuando hay diferencias en campos")
    void debeClasificarComoEditado() {
        Activo activoAdmin = crearActivo(1L, "ACT001", "Lenovo", "ABC123", OrigenActivo.ADMINISTRADOR);
        Activo activoInspector = crearActivo(2L, "ACT001", "HP", "ABC123", OrigenActivo.INSPECTOR);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.ADMINISTRADOR))
                .thenReturn(List.of(activoAdmin));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.INSPECTOR))
                .thenReturn(List.of(activoInspector));
        when(resultadoCruceRepository.save(any())).thenAnswer(i -> {
            ResultadoCruce rc = i.getArgument(0);
            rc.setId(1L);
            return rc;
        });
        when(detalleCruceRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        CruceResponse response = cruceInventarioService.ejecutarCruce(1L);

        assertNotNull(response);
        assertEquals(1, response.getResumen().getTotal());
        assertEquals(0, response.getResumen().getCruceNormal());
        assertEquals(1, response.getResumen().getEditado());
        assertEquals(0, response.getResumen().getSobrante());
        
        assertEquals("EDITADO", response.getActivos().get(0).getEstado());
        assertFalse(response.getActivos().get(0).getCamposModificados().isEmpty());
        assertTrue(response.getActivos().get(0).getCamposModificados().contains("marca"));
    }

    @Test
    @DisplayName("Debe clasificar como SOBRANTE cuando solo existe en inspector")
    void debeClasificarComoSobrante() {
        Activo activoInspector = crearActivo(2L, "ACT999", "Dell", "XYZ999", OrigenActivo.INSPECTOR);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.ADMINISTRADOR))
                .thenReturn(List.of());
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.INSPECTOR))
                .thenReturn(List.of(activoInspector));
        when(resultadoCruceRepository.save(any())).thenAnswer(i -> {
            ResultadoCruce rc = i.getArgument(0);
            rc.setId(1L);
            return rc;
        });
        when(detalleCruceRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        CruceResponse response = cruceInventarioService.ejecutarCruce(1L);

        assertNotNull(response);
        assertEquals(1, response.getResumen().getTotal());
        assertEquals(0, response.getResumen().getCruceNormal());
        assertEquals(0, response.getResumen().getEditado());
        assertEquals(1, response.getResumen().getSobrante());
        assertEquals("SOBRANTE", response.getActivos().get(0).getEstado());
    }

    @Test
    @DisplayName("Debe clasificar como FALTANTE cuando solo existe en administrador")
    void debeClasificarComoFaltante() {
        Activo activoAdmin = crearActivo(1L, "ACT001", "Lenovo", "ABC123", OrigenActivo.ADMINISTRADOR);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.ADMINISTRADOR))
                .thenReturn(List.of(activoAdmin));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.INSPECTOR))
                .thenReturn(List.of());
        when(resultadoCruceRepository.save(any())).thenAnswer(i -> {
            ResultadoCruce rc = i.getArgument(0);
            rc.setId(1L);
            return rc;
        });
        when(detalleCruceRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        CruceResponse response = cruceInventarioService.ejecutarCruce(1L);

        assertNotNull(response);
        assertEquals(1, response.getResumen().getTotal());
        assertEquals(0, response.getResumen().getCruceNormal());
        assertEquals(1, response.getResumen().getFaltante());
        assertEquals("FALTANTE", response.getActivos().get(0).getEstado());
    }

    @Test
    @DisplayName("Debe procesar correctamente un cruce con múltiples estados")
    void debeProcesarCruceCompleto() {
        Activo adminNormal = crearActivo(1L, "ACT001", "Lenovo", "ABC123", OrigenActivo.ADMINISTRADOR);
        Activo inspectorNormal = crearActivo(2L, "ACT001", "Lenovo", "ABC123", OrigenActivo.INSPECTOR);
        
        Activo adminEditado = crearActivo(3L, "ACT002", "HP", "DEF456", OrigenActivo.ADMINISTRADOR);
        Activo inspectorEditado = crearActivo(4L, "ACT002", "Dell", "DEF456", OrigenActivo.INSPECTOR);
        
        Activo adminFaltante = crearActivo(5L, "ACT003", "Acer", "GHI789", OrigenActivo.ADMINISTRADOR);
        
        Activo inspectorSobrante = crearActivo(6L, "ACT999", "Sony", "XYZ999", OrigenActivo.INSPECTOR);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.ADMINISTRADOR))
                .thenReturn(Arrays.asList(adminNormal, adminEditado, adminFaltante));
        when(activoRepository.findByInventarioIdAndOrigen(1L, OrigenActivo.INSPECTOR))
                .thenReturn(Arrays.asList(inspectorNormal, inspectorEditado, inspectorSobrante));
        when(resultadoCruceRepository.save(any())).thenAnswer(i -> {
            ResultadoCruce rc = i.getArgument(0);
            rc.setId(1L);
            return rc;
        });
        when(detalleCruceRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        CruceResponse response = cruceInventarioService.ejecutarCruce(1L);

        assertNotNull(response);
        assertEquals(4, response.getResumen().getTotal());
        assertEquals(1, response.getResumen().getCruceNormal());
        assertEquals(1, response.getResumen().getEditado());
        assertEquals(1, response.getResumen().getSobrante());
        assertEquals(1, response.getResumen().getFaltante());
    }

    /**
     * Método auxiliar para crear activos de prueba
     */
    private Activo crearActivo(Long id, String idActivo, String marca, String serial, OrigenActivo origen) {
        return Activo.builder()
                .id(id)
                .inventario(inventario)
                .idActivo(idActivo)
                .etiqueta("ETQ-" + idActivo)
                .descripcion("Descripción " + idActivo)
                .marca(marca)
                .serial(serial)
                .modelo("Modelo X")
                .responsable("Juan Pérez")
                .ciudad("Bogotá")
                .estado("Bueno")
                .origen(origen)
                .build();
    }
}
