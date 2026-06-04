package com.rti.inventarios.repository;

import com.rti.inventarios.model.entity.ResultadoCruce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de resultados de cruce.
 * 
 * Proporciona métodos para consultar los resultados de cruces realizados por inventario.
 */
@Repository
public interface ResultadoCruceRepository extends JpaRepository<ResultadoCruce, Long> {

    /**
     * Busca todos los resultados de cruce de un inventario específico
     * 
     * @param inventarioId ID del inventario
     * @return Lista de resultados de cruce ordenados por fecha descendente
     */
    List<ResultadoCruce> findByInventarioIdOrderByFechaCruceDesc(Long inventarioId);

    /**
     * Busca el último resultado de cruce de un inventario
     * 
     * @param inventarioId ID del inventario
     * @return Optional con el resultado más reciente si existe
     */
    Optional<ResultadoCruce> findFirstByInventarioIdOrderByFechaCruceDesc(Long inventarioId);

    /**
     * Elimina todos los resultados de cruce de un inventario
     * 
     * @param inventarioId ID del inventario
     */
    void deleteByInventarioId(Long inventarioId);
}
