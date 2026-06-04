package com.rti.inventarios.model.enums;

/**
 * Enumeración que define los estados posibles resultantes del cruce de información
 * entre los activos cargados por el administrador y los registrados por el inspector.
 * 
 * CRUCE_NORMAL: El activo existe tanto en la carga del administrador como en la del
 *               inspector, y todos los campos comparables son idénticos.
 * 
 * EDITADO: El activo existe en ambas fuentes pero al menos un campo comparable presenta
 *          diferencias entre la información del administrador y la del inspector.
 * 
 * SOBRANTE: El activo fue registrado por el inspector pero no existe en la carga
 *           inicial del administrador (activo adicional encontrado en campo).
 * 
 * FALTANTE: El activo fue cargado por el administrador pero no fue registrado por
 *           el inspector durante la inspección (activo no encontrado en campo).
 */
public enum EstadoCruce {
    /**
     * Activo coincide completamente entre ambas fuentes
     */
    CRUCE_NORMAL,
    
    /**
     * Activo existe en ambas fuentes pero con diferencias en sus datos
     */
    EDITADO,
    
    /**
     * Activo registrado solo por el inspector (no estaba en carga inicial)
     */
    SOBRANTE,
    
    /**
     * Activo cargado por administrador pero no encontrado por inspector
     */
    FALTANTE
}
