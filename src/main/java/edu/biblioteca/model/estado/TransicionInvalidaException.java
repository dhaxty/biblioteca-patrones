package edu.biblioteca.model.estado;

/** Se lanza cuando se pide una operación que el estado actual del ejemplar no permite. */
public class TransicionInvalidaException extends RuntimeException {

    private final TipoEstado estadoActual;
    private final String operacion;

    public TransicionInvalidaException(TipoEstado estadoActual, String operacion) {
        super("No se puede " + operacion + " un ejemplar en estado \"" + estadoActual.etiqueta() + "\".");
        this.estadoActual = estadoActual;
        this.operacion = operacion;
    }

    public TipoEstado getEstadoActual() {
        return estadoActual;
    }

    public String getOperacion() {
        return operacion;
    }
}
