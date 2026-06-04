package com.rti.inventarios.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un centro de costos o ubicación física donde se realizan
 * los inventarios de activos.
 * 
 * Cada centro tiene un código único que lo identifica y puede asociarse a uno o más
 * inventarios.
 */
@Entity
@Table(name = "centros_costos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CentroCostos {

    /**
     * Identificador único del centro de costos
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código único del centro de costos (ej: 014, 015)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    /**
     * Nombre descriptivo del centro de costos
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Ciudad donde se ubica el centro de costos
     */
    @Column(length = 100)
    private String ciudad;

    /**
     * Dirección física del centro de costos
     */
    @Column(length = 255)
    private String direccion;

    /**
     * Indica si el centro de costos está activo
     */
    @Column(nullable = false)
    private Boolean activo = true;
}
