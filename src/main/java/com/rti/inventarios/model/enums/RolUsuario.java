package com.rti.inventarios.model.enums;

/**
 * Enumeración que define los roles disponibles para los usuarios en el sistema.
 * 
 * ADMINISTRADOR: Usuario con permisos completos para gestionar usuarios, inventarios,
 *                cargar activos desde Excel y realizar cruces de información.
 * 
 * INSPECTOR: Usuario que puede validar inventarios asignados, consultar y registrar
 *            activos en campo desde la aplicación web o móvil.
 */
public enum RolUsuario {
    /**
     * Rol de administrador con permisos completos en el sistema
     */
    ADMINISTRADOR,
    
    /**
     * Rol de inspector para registro de activos en campo
     */
    INSPECTOR
}
