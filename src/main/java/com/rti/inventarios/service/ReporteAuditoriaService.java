package com.rti.inventarios.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rti.inventarios.exception.ResourceNotFoundException;
import com.rti.inventarios.model.entity.Activo;
import com.rti.inventarios.model.entity.DetalleCruce;
import com.rti.inventarios.model.entity.ResultadoCruce;
import com.rti.inventarios.model.enums.OrigenActivo;
import com.rti.inventarios.repository.ActivoRepository;
import com.rti.inventarios.repository.DetalleCruceRepository;
import com.rti.inventarios.repository.ResultadoCruceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Servicio para la generación de reportes en formato Excel.
 * 
 * Genera archivos Excel con los resultados del cruce de inventario,
 * activos del administrador y reportes de auditoría.
 */
@Service
public class ReporteAuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteAuditoriaService.class);

    @Autowired
    private ResultadoCruceRepository resultadoCruceRepository;

    @Autowired
    private DetalleCruceRepository detalleCruceRepository;

    @Autowired
    private ActivoRepository activoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Genera un archivo Excel con el resultado del cruce de inventario
     * 
     * @param inventarioId ID del inventario
     * @return Resource con el archivo Excel
     */
    @Transactional(readOnly = true)
    public Resource generarExcelCruce(Long inventarioId) throws IOException {
        logger.info("Generando Excel de cruce para inventario: {}", inventarioId);

        ResultadoCruce resultado = resultadoCruceRepository
                .findFirstByInventarioIdOrderByFechaCruceDesc(inventarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se ha ejecutado ningún cruce para el inventario: " + inventarioId
                ));

        List<DetalleCruce> detalles = detalleCruceRepository.findByResultadoCruceId(resultado.getId());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Resultado Cruce");

        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle normalStyle = crearEstiloNormal(workbook, IndexedColors.LIGHT_GREEN);
        CellStyle editadoStyle = crearEstiloNormal(workbook, IndexedColors.LIGHT_YELLOW);
        CellStyle sobranteStyle = crearEstiloNormal(workbook, IndexedColors.LIGHT_ORANGE);
        CellStyle faltanteStyle = crearEstiloNormal(workbook, IndexedColors.LIGHT_CORNFLOWER_BLUE);

        String[] columnas = {
                "ID_ACTIVO", "ETIQUETA", "DESCRIPCION", "MARCA", "SERIAL",
                "MODELO", "RESPONSABLE", "CIUDAD", "ESTADO", "CAMPOS_MODIFICADOS"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, i == columnas.length - 1 ? 6000 : 4000);
        }

        int rowNum = 1;
        for (DetalleCruce detalle : detalles) {
            Row row = sheet.createRow(rowNum++);

            CellStyle style = switch (detalle.getEstadoCruce()) {
                case CRUCE_NORMAL -> normalStyle;
                case EDITADO -> editadoStyle;
                case SOBRANTE -> sobranteStyle;
                case FALTANTE -> faltanteStyle;
            };

            crearCeldaConEstilo(row, 0, detalle.getIdActivo(), style);
            crearCeldaConEstilo(row, 1, detalle.getEtiqueta(), style);
            crearCeldaConEstilo(row, 2, detalle.getDescripcion(), style);
            crearCeldaConEstilo(row, 3, detalle.getMarca(), style);
            crearCeldaConEstilo(row, 4, detalle.getSerial(), style);
            crearCeldaConEstilo(row, 5, detalle.getModelo(), style);
            crearCeldaConEstilo(row, 6, detalle.getResponsable(), style);
            crearCeldaConEstilo(row, 7, detalle.getCiudad(), style);

            String estadoStr = detalle.getEstadoCruce().name().replace("_", " ");
            crearCeldaConEstilo(row, 8, estadoStr, style);

            String camposModificados = formatearCamposModificados(detalle.getCamposModificados());
            crearCeldaConEstilo(row, 9, camposModificados, style);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        logger.info("Excel de cruce generado con {} registros", detalles.size());

        return new ByteArrayResource(outputStream.toByteArray());
    }

    /**
     * Genera un archivo Excel solo con los activos del administrador
     * 
     * @param inventarioId ID del inventario
     * @return Resource con el archivo Excel
     */
    @Transactional(readOnly = true)
    public Resource generarExcelActivosAdministrador(Long inventarioId) throws IOException {
        logger.info("Generando Excel de activos administrador para inventario: {}", inventarioId);

        List<Activo> activos = activoRepository.findByInventarioIdAndOrigen(
                inventarioId, OrigenActivo.ADMINISTRADOR
        );

        if (activos.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No hay activos de administrador para el inventario: " + inventarioId
            );
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Activos Administrador");

        CellStyle headerStyle = crearEstiloEncabezado(workbook);

        String[] columnas = {
                "ID_ACTIVO", "ETIQUETA", "DESCRIPCION", "MARCA", "SERIAL",
                "MODELO", "RESPONSABLE", "CIUDAD", "ESTADO"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        int rowNum = 1;
        for (Activo activo : activos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(activo.getIdActivo());
            row.createCell(1).setCellValue(activo.getEtiqueta());
            row.createCell(2).setCellValue(activo.getDescripcion());
            row.createCell(3).setCellValue(activo.getMarca());
            row.createCell(4).setCellValue(activo.getSerial());
            row.createCell(5).setCellValue(activo.getModelo());
            row.createCell(6).setCellValue(activo.getResponsable());
            row.createCell(7).setCellValue(activo.getCiudad());
            row.createCell(8).setCellValue(activo.getEstado());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        logger.info("Excel generado con {} activos del administrador", activos.size());

        return new ByteArrayResource(outputStream.toByteArray());
    }

    /**
     * Crea un estilo para el encabezado de las columnas
     */
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crea un estilo normal con color de fondo específico
     */
    private CellStyle crearEstiloNormal(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crea una celda con valor y estilo
     */
    private void crearCeldaConEstilo(Row row, int columna, String valor, CellStyle style) {
        Cell cell = row.createCell(columna);
        cell.setCellValue(valor != null ? valor : "");
        cell.setCellStyle(style);
    }

    /**
     * Formatea la lista de campos modificados para mostrar en Excel
     */
    private String formatearCamposModificados(String camposJson) {
        if (camposJson == null || camposJson.equals("[]")) {
            return "";
        }

        try {
            List<String> campos = objectMapper.readValue(camposJson, new TypeReference<List<String>>() {});
            return String.join(", ", campos);
        } catch (JsonProcessingException e) {
            logger.warn("Error al deserializar campos modificados", e);
            return "";
        }
    }
}
