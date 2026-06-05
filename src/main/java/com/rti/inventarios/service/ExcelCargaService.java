package com.rti.inventarios.service;

import com.rti.inventarios.exception.BusinessException;
import com.rti.inventarios.exception.ResourceNotFoundException;
import com.rti.inventarios.model.dto.CargaExcelResponse;
import com.rti.inventarios.model.entity.Activo;
import com.rti.inventarios.model.entity.Inventario;
import com.rti.inventarios.model.enums.OrigenActivo;
import com.rti.inventarios.repository.ActivoRepository;
import com.rti.inventarios.repository.InventarioRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la carga masiva de activos desde archivos Excel.
 * 
 * Maneja la generación de plantillas Excel y el procesamiento de archivos
 * cargados por el administrador con información inicial de activos.
 */
@Service
public class ExcelCargaService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelCargaService.class);

    private static final String[] COLUMNAS_PLANTILLA = {
            "ID_ACTIVO", "ETIQUETA", "DESCRIPCION", "MARCA", "SERIAL",
            "MODELO", "RESPONSABLE", "CIUDAD", "ESTADO"
    };

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ActivoRepository activoRepository;

    /**
     * Genera una plantilla Excel vacía con las columnas requeridas
     * 
     * @return Resource con el archivo Excel de plantilla
     */
    public Resource generarPlantilla() throws IOException {
        logger.debug("Generando plantilla Excel");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Activos");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < COLUMNAS_PLANTILLA.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(COLUMNAS_PLANTILLA[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        byte[] bytes = outputStream.toByteArray();
        logger.info("Plantilla Excel generada con {} columnas", COLUMNAS_PLANTILLA.length);

        return new ByteArrayResource(bytes);
    }

    /**
     * Procesa un archivo Excel y carga los activos en el inventario especificado
     * 
     * @param inventarioId ID del inventario
     * @param archivo Archivo Excel con los activos
     * @return Resultado de la carga con estadísticas
     */
    @Transactional
    public CargaExcelResponse cargarExcel(Long inventarioId, MultipartFile archivo) throws IOException {
        logger.info("Iniciando carga de Excel para inventario: {}", inventarioId);

        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario no encontrado con ID: " + inventarioId
                ));

        if (!archivo.getOriginalFilename().endsWith(".xlsx") && 
            !archivo.getOriginalFilename().endsWith(".xls")) {
            throw new BusinessException("El archivo debe ser un Excel (.xlsx o .xls)");
        }

        List<String> errores = new ArrayList<>();
        int registrosProcesados = 0;
        int registrosGuardados = 0;

        try (InputStream is = archivo.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("El archivo Excel está vacío");
            }

            if (!validarColumnas(headerRow)) {
                throw new BusinessException("Estructura de Excel inválida. " +
                        "Las columnas requeridas son: " + String.join(", ", COLUMNAS_PLANTILLA));
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || esFilaVacia(row)) {
                    continue;
                }

                registrosProcesados++;

                try {
                    Activo activo = procesarFila(row, inventario);
                    
                    // Verificar si el activo ya existe en este inventario (origen ADMINISTRADOR)
                    Optional<Activo> activoExistente = activoRepository.findByInventarioIdAndIdActivoAndOrigen(
                            inventario.getId(),
                            activo.getIdActivo(),
                            OrigenActivo.ADMINISTRADOR
                    );
                    
                    if (activoExistente.isPresent()) {
                        // Actualizar el activo existente
                        Activo existente = activoExistente.get();
                        existente.setEtiqueta(activo.getEtiqueta());
                        existente.setDescripcion(activo.getDescripcion());
                        existente.setMarca(activo.getMarca());
                        existente.setSerial(activo.getSerial());
                        existente.setModelo(activo.getModelo());
                        existente.setResponsable(activo.getResponsable());
                        existente.setCiudad(activo.getCiudad());
                        existente.setEstado(activo.getEstado());
                        activoRepository.save(existente);
                        logger.debug("Activo actualizado: {}", activo.getIdActivo());
                    } else {
                        // Crear nuevo activo
                        activoRepository.save(activo);
                        logger.debug("Activo creado: {}", activo.getIdActivo());
                    }
                    
                    registrosGuardados++;
                } catch (Exception e) {
                    String error = "Fila " + (i + 1) + ": " + e.getMessage();
                    errores.add(error);
                    logger.warn(error);
                }
            }
        }

        logger.info("Carga finalizada. Procesados: {}, Guardados: {}, Errores: {}", 
                    registrosProcesados, registrosGuardados, errores.size());

        return CargaExcelResponse.builder()
                .mensaje(errores.isEmpty() ? "Carga exitosa" : "Carga completada con errores")
                .registrosProcesados(registrosProcesados)
                .registrosGuardados(registrosGuardados)
                .errores(errores)
                .build();
    }

    /**
     * Valida que el archivo Excel tenga las columnas requeridas
     */
    private boolean validarColumnas(Row headerRow) {
        if (headerRow.getLastCellNum() < COLUMNAS_PLANTILLA.length) {
            return false;
        }

        for (int i = 0; i < COLUMNAS_PLANTILLA.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !COLUMNAS_PLANTILLA[i].equals(cell.getStringCellValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifica si una fila está vacía
     */
    private boolean esFilaVacia(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Procesa una fila del Excel y crea una entidad Activo
     */
    private Activo procesarFila(Row row, Inventario inventario) {
        String idActivo = getCellValueAsString(row.getCell(0));
        
        if (idActivo == null || idActivo.isBlank()) {
            throw new BusinessException("ID_ACTIVO vacío");
        }

        return Activo.builder()
                .inventario(inventario)
                .idActivo(idActivo.trim())
                .etiqueta(getCellValueAsString(row.getCell(1)))
                .descripcion(getCellValueAsString(row.getCell(2)))
                .marca(getCellValueAsString(row.getCell(3)))
                .serial(getCellValueAsString(row.getCell(4)))
                .modelo(getCellValueAsString(row.getCell(5)))
                .responsable(getCellValueAsString(row.getCell(6)))
                .ciudad(getCellValueAsString(row.getCell(7)))
                .estado(getCellValueAsString(row.getCell(8)))
                .origen(OrigenActivo.ADMINISTRADOR)
                .build();
    }

    /**
     * Extrae el valor de una celda como String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
}
