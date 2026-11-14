package edu.biblioteca.model.estado;

/** Se lanza cuando se pide una operación que el estado actual del ejemplar no permite. */
public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(TipoEstado estadoActual, String operacion) {
        super("No se puede " + operacion + " un ejemplar en estado \"" + estadoActual.etiqueta() + "\".");
    }
}
