package com.rti.inventarios.repository;

import com.rti.inventarios.model.entity.Activo;
import com.rti.inventarios.model.enums.OrigenActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de activos.
 * 
 * Proporciona métodos para consultar activos por inventario, origen y código de activo.
 */
@Repository
public interface ActivoRepository extends JpaRepository<Activo, Long> {

    /**
     * Busca todos los activos de un inventario específico
     * 
     * @param inventarioId ID del inventario
     * @return Lista de activos del inventario
     */
    List<Activo> findByInventarioId(Long inventarioId);

    /**
     * Busca activos de un inventario filtrados por origen
     * 
     * @param inventarioId ID del inventario
     * @param origen Origen del activo (ADMINISTRADOR o INSPECTOR)
     * @return Lista de activos filtrados
     */
    List<Activo> findByInventarioIdAndOrigen(Long inventarioId, OrigenActivo origen);

    /**
     * Busca un activo específico por inventario, código y origen
     * 
     * @param inventarioId ID del inventario
     * @param idActivo Código del activo
     * @param origen Origen del activo
     * @return Optional con el activo si existe
     */
    Optional<Activo> findByInventarioIdAndIdActivoAndOrigen(
            Long inventarioId, 
            String idActivo, 
            OrigenActivo origen
    );

    /**
     * Busca un activo por su etiqueta/barcode en un inventario específico
     * 
     * @param inventarioId ID del inventario
     * @param etiqueta Etiqueta o código de barras
     * @param origen Origen del activo
     * @return Optional con el activo si existe
     */
    Optional<Activo> findByInventarioIdAndEtiquetaAndOrigen(
            Long inventarioId,
            String etiqueta,
            OrigenActivo origen
    );

    /**
     * Cuenta los activos de un inventario por origen
     * 
     * @param inventarioId ID del inventario
     * @param origen Origen del activo
     * @return Cantidad de activos
     */
    long countByInventarioIdAndOrigen(Long inventarioId, OrigenActivo origen);

    /**
     * Elimina todos los activos de un inventario con un origen específico
     * 
     * @param inventarioId ID del inventario
     * @param origen Origen de los activos a eliminar
     */
    void deleteByInventarioIdAndOrigen(Long inventarioId, OrigenActivo origen);
}
