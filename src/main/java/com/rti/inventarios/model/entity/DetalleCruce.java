package com.rti.inventarios.model.entity;

import com.rti.inventarios.model.enums.EstadoCruce;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que almacena el detalle de cada activo procesado en un cruce de información.
 * 
 * Registra el estado final del activo (CRUCE_NORMAL, EDITADO, SOBRANTE, FALTANTE)
 * y en caso de estado EDITADO, almacena los nombres de los campos que presentaron diferencias.
 */
@Entity
@Table(name = "detalle_cruce")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCruce {

    /**
     * Identificador único del detalle
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Resultado de cruce al que pertenece este detalle
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_cruce_id", nullable = false)
    private ResultadoCruce resultadoCruce;

    /**
     * Identificador del activo
     */
    @Column(name = "id_activo", nullable = false, length = 100)
    private String idActivo;

    /**
     * Etiqueta o código de barras del activo
     */
    @Column(length = 255)
    private String etiqueta;

    /**
     * Descripción del activo
     */
    @Column(length = 500)
    private String descripcion;

    /**
     * Marca del activo
     */
    @Column(length = 100)
    private String marca;

    /**
     * Número de serie del activo
     */
    @Column(length = 100)
    private String serial;

    /**
     * Modelo del activo
     */
    @Column(length = 100)
    private String modelo;

    /**
     * Persona responsable del activo
     */
    @Column(length = 255)
    private String responsable;

    /**
     * Ciudad donde se encuentra el activo
     */
    @Column(length = 100)
    private String ciudad;

    /**
     * Estado resultante del cruce para este activo
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cruce", nullable = false)
    private EstadoCruce estadoCruce;

    /**
     * Lista de campos modificados en formato JSON (solo para estado EDITADO)
     * Ejemplo: ["marca", "serial", "responsable"]
     */
    @Column(name = "campos_modificados", columnDefinition = "TEXT")
    private String camposModificados;
}
