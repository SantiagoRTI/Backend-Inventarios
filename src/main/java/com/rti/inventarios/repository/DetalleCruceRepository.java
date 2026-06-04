package com.rti.inventarios.repository;

import com.rti.inventarios.model.entity.DetalleCruce;
import com.rti.inventarios.model.enums.EstadoCruce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de persistencia del detalle de cruce.
 * 
 * Proporciona métodos para consultar los detalles de activos procesados en un cruce.
 */
@Repository
public interface DetalleCruceRepository extends JpaRepository<DetalleCruce, Long> {

    /**
     * Busca todos los detalles de un resultado de cruce específico
     * 
     * @param resultadoCruceId ID del resultado de cruce
     * @return Lista de detalles del cruce
     */
    List<DetalleCruce> findByResultadoCruceId(Long resultadoCruceId);

    /**
     * Busca detalles de cruce por estado
     * 
     * @param resultadoCruceId ID del resultado de cruce
     * @param estadoCruce Estado del cruce a filtrar
     * @return Lista de detalles con ese estado
     */
    List<DetalleCruce> findByResultadoCruceIdAndEstadoCruce(
            Long resultadoCruceId,
            EstadoCruce estadoCruce
    );

    /**
     * Elimina todos los detalles de un resultado de cruce
     * 
     * @param resultadoCruceId ID del resultado de cruce
     */
    void deleteByResultadoCruceId(Long resultadoCruceId);
}
