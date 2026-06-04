package com.rti.inventarios.repository;

import com.rti.inventarios.model.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de inventarios.
 * 
 * Proporciona métodos para consultar inventarios por código, inspector y estado.
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    /**
     * Busca un inventario por su código único
     * 
     * @param codigo Código del inventario
     * @return Optional con el inventario si existe
     */
    Optional<Inventario> findByCodigo(String codigo);

    /**
     * Verifica si existe un inventario con el código dado
     * 
     * @param codigo Código a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca inventarios asignados a un inspector específico
     * 
     * @param inspectorId ID del inspector
     * @return Lista de inventarios asignados
     */
    List<Inventario> findByInspectorId(Long inspectorId);

    /**
     * Busca inventarios por estado
     * 
     * @param estado Estado del inventario
     * @return Lista de inventarios con ese estado
     */
    List<Inventario> findByEstado(String estado);

    /**
     * Busca inventarios activos
     * 
     * @param activo Estado de activación
     * @return Lista de inventarios activos o inactivos
     */
    List<Inventario> findByActivo(Boolean activo);
}
