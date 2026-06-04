package com.rti.inventarios.exception;

/**
 * Excepción lanzada cuando hay un error en la lógica de negocio.
 * 
 * Por ejemplo, cuando se intenta realizar una operación inválida según
 * las reglas del negocio del sistema.
 */
public class BusinessException extends RuntimeException {

    /**
     * Constructor con mensaje de error
     * 
     * @param mensaje Descripción del error de negocio
     */
    public BusinessException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa
     * 
     * @param mensaje Descripción del error de negocio
     * @param causa Excepción que causó este error
     */
    public BusinessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
