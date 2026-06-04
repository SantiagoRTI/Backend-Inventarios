package com.rti.inventarios.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado en el sistema.
 * 
 * Por ejemplo, cuando se busca un usuario, inventario o activo que no existe.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje de error
     * 
     * @param mensaje Descripción del recurso no encontrado
     */
    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa
     * 
     * @param mensaje Descripción del recurso no encontrado
     * @param causa Excepción que causó este error
     */
    public ResourceNotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
