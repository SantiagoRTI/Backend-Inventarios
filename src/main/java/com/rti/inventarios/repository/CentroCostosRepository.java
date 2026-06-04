package com.rti.inventarios.repository;

import com.rti.inventarios.model.entity.CentroCostos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de centros de costos.
 * 
 * Proporciona métodos para consultar centros de costos por código y estado.
 */
@Repository
public interface CentroCostosRepository extends JpaRepository<CentroCostos, Long> {

    /**
     * Busca un centro de costos por su código único
     * 
     * @param codigo Código del centro de costos
     * @return Optional con el centro si existe
     */
    Optional<CentroCostos> findByCodigo(String codigo);

    /**
     * Verifica si existe un centro de costos con el código dado
     * 
     * @param codigo Código a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigo(String codigo);
}
