package com.rti.inventarios.model.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la respuesta del proceso de carga de activos desde archivo Excel.
 * 
 * Proporciona información sobre el resultado de la carga masiva incluyendo
 * estadísticas y posibles errores encontrados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargaExcelResponse {

    /**
     * Mensaje general del resultado de la carga
     */
    private String mensaje;

    /**
     * Cantidad de registros procesados del archivo Excel
     */
    private Integer registrosProcesados;

    /**
     * Cantidad de registros guardados exitosamente en la base de datos
     */
    private Integer registrosGuardados;

    /**
     * Lista de errores encontrados durante el procesamiento
     */
    @Builder.Default
    private List<String> errores = new ArrayList<>();
}
