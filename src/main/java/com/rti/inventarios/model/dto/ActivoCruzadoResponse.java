package com.rti.inventarios.model.dto;

import lombok.*;

import java.util.List;

/**
 * DTO que representa un activo individual procesado en el cruce.
 * 
 * Incluye todos los datos del activo, su estado de cruce y en caso de ser EDITADO,
 * la lista de campos que presentaron diferencias.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivoCruzadoResponse {

    /**
     * Identificador del activo
     */
    private String idActivo;

    /**
     * Etiqueta o código de barras
     */
    private String etiqueta;

    /**
     * Descripción del activo
     */
    private String descripcion;

    /**
     * Marca del activo
     */
    private String marca;

    /**
     * Número de serie
     */
    private String serial;

    /**
     * Modelo del activo
     */
    private String modelo;

    /**
     * Persona responsable
     */
    private String responsable;

    /**
     * Ciudad donde se encuentra
     */
    private String ciudad;

    /**
     * Estado resultante del cruce (CRUCE NORMAL, EDITADO, SOBRANTE, FALTANTE)
     */
    private String estado;

    /**
     * Lista de campos modificados (solo para estado EDITADO)
     */
    private List<String> camposModificados;
}
