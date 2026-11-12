package edu.biblioteca.model.estado;

import edu.biblioteca.model.Ejemplar;

public final class EstadoPrestado implements EstadoEjemplar {

    public static final EstadoPrestado INSTANCIA = new EstadoPrestado();

    private EstadoPrestado() {
    }

    @Override
    public TipoEstado tipo() {
        return TipoEstado.PRESTADO;
    }

    @Override
    public void prestar(Ejemplar ejemplar) {
        throw new TransicionInvalidaException(tipo(), "prestar");
    }

    @Override
    public void devolver(Ejemplar ejemplar, boolean conDanio) {
        ejemplar.cambiarEstado(conDanio ? EstadoEnMantenimiento.INSTANCIA : EstadoDisponible.INSTANCIA);
    }

    @Override
    public void enviarAMantenimiento(Ejemplar ejemplar) {
        // Un ejemplar prestado primero debe devolverse (con daño si corresponde).
        throw new TransicionInvalidaException(tipo(), "enviar a mantenimiento");
    }

    @Override
    public void habilitar(Ejemplar ejemplar) {
        throw new TransicionInvalidaException(tipo(), "habilitar");
    }
}
