package biblioteca.model;

import biblioteca.BaseDatosDePrueba;
import biblioteca.model.estado.EstadoDisponible;
import biblioteca.model.estado.EstadoEjemplar;
import biblioteca.model.estado.EstadoEnMantenimiento;
import biblioteca.model.estado.EstadoPrestado;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadoEjemplarTest {

    private Ejemplar nuevoEjemplar() {
        return new Ejemplar("EJ-001", BaseDatosDePrueba.libroEjemplo("9780306406157"));
    }

    @Test
    void ejemplarNuevoEstaDisponible() {
        assertEquals("DISPONIBLE", nuevoEjemplar().getEstadoNombre());
    }

    @Test
    void cicloPrestarYDevolver() {
        Ejemplar e = nuevoEjemplar();
        e.prestar();
        assertEquals("PRESTADO", e.getEstadoNombre());
        e.devolver();
        assertEquals("DISPONIBLE", e.getEstadoNombre());
    }

    @Test
    void cicloMantenimientoYHabilitacion() {
        Ejemplar e = nuevoEjemplar();
        e.enviarAMantenimiento();
        assertEquals("EN_MANTENIMIENTO", e.getEstadoNombre());
        e.habilitar();
        assertEquals("DISPONIBLE", e.getEstadoNombre());
    }

    @Test
    void disponibleRechazaDevolverYHabilitar() {
        Ejemplar e = nuevoEjemplar();
        assertThrows(IllegalStateException.class, e::devolver);
        assertThrows(IllegalStateException.class, e::habilitar);
        assertEquals("DISPONIBLE", e.getEstadoNombre());
    }

    @Test
    void prestadoRechazaPrestarMantenimientoYHabilitar() {
        Ejemplar e = nuevoEjemplar();
        e.prestar();
        assertThrows(IllegalStateException.class, e::prestar);
        assertThrows(IllegalStateException.class, e::enviarAMantenimiento);
        assertThrows(IllegalStateException.class, e::habilitar);
        assertEquals("PRESTADO", e.getEstadoNombre());
    }

    @Test
    void enMantenimientoRechazaPrestarDevolverYReenviar() {
        Ejemplar e = nuevoEjemplar();
        e.enviarAMantenimiento();
        assertThrows(IllegalStateException.class, e::prestar);
        assertThrows(IllegalStateException.class, e::devolver);
        assertThrows(IllegalStateException.class, e::enviarAMantenimiento);
        assertEquals("EN_MANTENIMIENTO", e.getEstadoNombre());
    }

    @Test
    void desdeNombreReconstruyeCadaEstado() {
        assertInstanceOf(EstadoDisponible.class, EstadoEjemplar.desdeNombre("DISPONIBLE"));
        assertInstanceOf(EstadoPrestado.class, EstadoEjemplar.desdeNombre("PRESTADO"));
        assertInstanceOf(EstadoEnMantenimiento.class, EstadoEjemplar.desdeNombre("EN_MANTENIMIENTO"));
    }

    @Test
    void desdeNombreRechazaValoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> EstadoEjemplar.desdeNombre(null));
        assertThrows(IllegalArgumentException.class, () -> EstadoEjemplar.desdeNombre(" "));
        assertThrows(IllegalArgumentException.class, () -> EstadoEjemplar.desdeNombre("PERDIDO"));
    }

    @Test
    void ejemplarValidaSusDatos() {
        Libro libro = BaseDatosDePrueba.libroEjemplo("9780306406157");
        assertThrows(IllegalArgumentException.class, () -> new Ejemplar(" ", libro));
        assertThrows(IllegalArgumentException.class, () -> new Ejemplar("EJ-1", null));
    }
}
