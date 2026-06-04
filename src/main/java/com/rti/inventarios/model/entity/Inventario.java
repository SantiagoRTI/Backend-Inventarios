package com.rti.inventarios.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un proceso de inventario de activos.
 * 
 * Cada inventario tiene un código único, puede estar asignado a un inspector específico
 * y asociado a un centro de costos. El estado indica si está en ejecución, finalizado, etc.
 */
@Entity
@Table(name = "inventarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    /**
     * Identificador único del inventario
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo del inventario
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Código único del inventario (ej: INV-014)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    /**
     * Estado actual del inventario (Ejecución, Finalizado, etc.)
     */
    @Column(nullable = false, length = 50)
    private String estado = "Ejecucion";

    /**
     * Inspector asignado a este inventario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private Usuario inspector;

    /**
     * Centro de costos asociado al inventario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_costos_id")
    private CentroCostos centroCostos;

    /**
     * Fecha y hora de creación del inventario
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de última actualización
     */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Fecha y hora de finalización del inventario
     */
    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    /**
     * Indica si el inventario está activo
     */
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Inicializa las fechas de auditoría antes de persistir
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de modificación antes de actualizar
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
