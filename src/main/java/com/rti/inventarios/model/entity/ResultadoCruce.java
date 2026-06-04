package com.rti.inventarios.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que almacena el resultado resumen de un proceso de cruce de información
 * entre los activos cargados por el administrador y los registrados por el inspector.
 * 
 * Contiene contadores de cada tipo de estado (CRUCE_NORMAL, EDITADO, SOBRANTE, FALTANTE)
 * y la fecha en que se realizó el cruce.
 */
@Entity
@Table(name = "resultados_cruce")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoCruce {

    /**
     * Identificador único del resultado del cruce
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Inventario al que corresponde este cruce
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    /**
     * Fecha y hora en que se ejecutó el cruce
     */
    @Column(name = "fecha_cruce", nullable = false)
    private LocalDateTime fechaCruce;

    /**
     * Total de activos procesados en el cruce
     */
    @Column(name = "total_activos")
    private Integer totalActivos = 0;

    /**
     * Cantidad de activos con estado CRUCE_NORMAL
     */
    @Column(name = "cruce_normal")
    private Integer cruceNormal = 0;

    /**
     * Cantidad de activos con estado EDITADO
     */
    @Column(name = "editados")
    private Integer editados = 0;

    /**
     * Cantidad de activos con estado SOBRANTE
     */
    @Column(name = "sobrantes")
    private Integer sobrantes = 0;

    /**
     * Cantidad de activos con estado FALTANTE
     */
    @Column(name = "faltantes")
    private Integer faltantes = 0;

    /**
     * Inicializa la fecha del cruce antes de persistir
     */
    @PrePersist
    protected void onCreate() {
        if (fechaCruce == null) {
            fechaCruce = LocalDateTime.now();
        }
    }
}
