package com.rti.inventarios.model.entity;

import com.rti.inventarios.model.enums.OrigenActivo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un activo registrado en el sistema.
 * 
 * Los activos pueden tener dos orígenes:
 * - ADMINISTRADOR: Cargados desde archivo Excel por el administrador
 * - INSPECTOR: Registrados en campo por el inspector durante la inspección
 * 
 * La combinación de inventarioId + idActivo + origen es única en el sistema.
 */
@Entity
@Table(name = "activos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activo {

    /**
     * Identificador único del registro
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Inventario al que pertenece este activo
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    /**
     * Identificador del activo (clave de negocio para el cruce)
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
     * Estado físico del activo
     */
    @Column(length = 50)
    private String estado;

    /**
     * Origen del registro (ADMINISTRADOR o INSPECTOR)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigenActivo origen;

    /**
     * Fecha y hora de registro del activo
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de última actualización
     */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

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
