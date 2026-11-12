package edu.biblioteca.model.estado;

import edu.biblioteca.model.Ejemplar;

public final class EstadoDisponible implements EstadoEjemplar {

    public static final EstadoDisponible INSTANCIA = new EstadoDisponible();

    private EstadoDisponible() {
    }

    @Override
    public TipoEstado tipo() {
        return TipoEstado.DISPONIBLE;
    }

    @Override
    public void prestar(Ejemplar ejemplar) {
        ejemplar.cambiarEstado(EstadoPrestado.INSTANCIA);
    }

    @Override
    public void devolver(Ejemplar ejemplar, boolean conDanio) {
        throw new TransicionInvalidaException(tipo(), "devolver");
    }

    @Override
    public void enviarAMantenimiento(Ejemplar ejemplar) {
        ejemplar.cambiarEstado(EstadoEnMantenimiento.INSTANCIA);
    }

    @Override
    public void habilitar(Ejemplar ejemplar) {
        throw new TransicionInvalidaException(tipo(), "habilitar");
    }
}
