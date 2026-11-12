package edu.biblioteca.model.estado;

import edu.biblioteca.model.Ejemplar;

public final class EstadoEnMantenimiento implements EstadoEjemplar {

    public static final EstadoEnMantenimiento INSTANCIA = new EstadoEnMantenimiento();

    private EstadoEnMantenimiento() {
    }

    @Override
    public TipoEstado tipo() {
        return TipoEstado.EN_MANTENIMIENTO;
    }

    @Override
    public void prestar(Ejemplar ejemplar) {
        throw new TransicionInvalidaException(tipo(), "prestar");
    }

    @Override
    public void devolver(Ejemplar ejemplar, boolean conDanio) {
        throw new TransicionInvalidaException(tipo(), "devolver");
    }

    @Override
    public void enviarAMantenimiento(Ejemplar ejemplar) {
        throw new TransicionInvalidaException(tipo(), "enviar a mantenimiento");
    }

    @Override
    public void habilitar(Ejemplar ejemplar) {
        ejemplar.cambiarEstado(EstadoDisponible.INSTANCIA);
    }
}
