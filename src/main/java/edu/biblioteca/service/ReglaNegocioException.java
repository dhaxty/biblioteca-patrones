package edu.biblioteca.service;

/** Error de regla de negocio con un mensaje apto para mostrarse al usuario en un JOptionPane. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
