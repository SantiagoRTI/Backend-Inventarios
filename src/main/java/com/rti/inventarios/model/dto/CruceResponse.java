package com.rti.inventarios.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta del proceso de cruce de información.
 * 
 * Contiene el resumen estadístico y el detalle de todos los activos procesados
 * con su respectivo estado de cruce.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CruceResponse {

    /**
     * ID del inventario al que corresponde el cruce
     */
    private Long inventarioId;

    /**
     * Fecha y hora en que se ejecutó el cruce
     */
    private LocalDateTime fechaCruce;

    /**
     * Resumen estadístico del cruce
     */
    private ResumenCruce resumen;

    /**
     * Lista de activos con su estado de cruce
     */
    private List<ActivoCruzadoResponse> activos;

    /**
     * Clase interna para el resumen del cruce
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumenCruce {
        /**
         * Total de activos procesados
         */
        private Integer total;

        /**
         * Cantidad de activos con cruce normal
         */
        private Integer cruceNormal;

        /**
         * Cantidad de activos editados
         */
        private Integer editado;

        /**
         * Cantidad de activos sobrantes
         */
        private Integer sobrante;

        /**
         * Cantidad de activos faltantes
         */
        private Integer faltante;
    }
}
