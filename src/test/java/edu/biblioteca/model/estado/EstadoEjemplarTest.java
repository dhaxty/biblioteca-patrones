package edu.biblioteca.model.estado;

import edu.biblioteca.model.Ejemplar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadoEjemplarTest {

    private Ejemplar nuevo() {
        return new Ejemplar("EJ-001", 1L);
    }

    @Test
    void nacePorDefectoDisponible() {
        assertEquals(TipoEstado.DISPONIBLE, nuevo().getTipoEstado());
    }

    @Test
    void prestarYDevolverNormal() {
        Ejemplar e = nuevo();
        e.prestar();
        assertEquals(TipoEstado.PRESTADO, e.getTipoEstado());
        e.devolver();
        assertEquals(TipoEstado.DISPONIBLE, e.getTipoEstado());
    }

    @Test
    void devolucionDanadaVaAMantenimiento() {
        Ejemplar e = nuevo();
        e.prestar();
        e.devolverConDanio();
        assertEquals(TipoEstado.EN_MANTENIMIENTO, e.getTipoEstado());
    }

    @Test
    void retiroYHabilitacion() {
        Ejemplar e = nuevo();
        e.enviarAMantenimiento();
        assertEquals(TipoEstado.EN_MANTENIMIENTO, e.getTipoEstado());
        e.habilitar();
        assertEquals(TipoEstado.DISPONIBLE, e.getTipoEstado());
    }

    @Test
    void rechazaTransicionesInvalidas() {
        Ejemplar disponible = nuevo();
        assertThrows(TransicionInvalidaException.class, disponible::devolver);
        assertThrows(TransicionInvalidaException.class, disponible::habilitar);

        Ejemplar prestado = nuevo();
        prestado.prestar();
        assertThrows(TransicionInvalidaException.class, prestado::prestar);
        assertThrows(TransicionInvalidaException.class, prestado::enviarAMantenimiento);
        assertThrows(TransicionInvalidaException.class, prestado::habilitar);

        Ejemplar enMantenimiento = nuevo();
        enMantenimiento.enviarAMantenimiento();
        assertThrows(TransicionInvalidaException.class, enMantenimiento::prestar);
        assertThrows(TransicionInvalidaException.class, enMantenimiento::devolver);
        assertThrows(TransicionInvalidaException.class, enMantenimiento::enviarAMantenimiento);
    }

    @Test
    void reconstruyeEstadoDesdeLaBaseDeDatos() {
        Ejemplar e = new Ejemplar(5L, "EJ-005", 1L, TipoEstado.PRESTADO);
        e.devolver();
        assertEquals(TipoEstado.DISPONIBLE, e.getTipoEstado());
    }
}
