package com.rti.inventarios.model.enums;

/**
 * Enumeración que identifica el origen de carga de un activo en el sistema.
 * 
 * ADMINISTRADOR: El activo fue cargado inicialmente por el administrador mediante
 *                archivo Excel con la plantilla oficial.
 * 
 * INSPECTOR: El activo fue registrado por un inspector durante el proceso de
 *            inspección en campo utilizando la aplicación web o móvil.
 */
public enum OrigenActivo {
    /**
     * Activo cargado por el administrador desde archivo Excel
     */
    ADMINISTRADOR,
    
    /**
     * Activo registrado por el inspector en campo
     */
    INSPECTOR
}
