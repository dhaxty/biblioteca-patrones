package biblioteca.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroMantenimientoTest {

    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 1, 10, 9, 0);

    @Test
    void registroNuevoEstaAbierto() {
        RegistroMantenimiento r = new RegistroMantenimiento(1L, "Lomo dañado", INGRESO);
        assertTrue(r.estaAbierto());
        assertEquals("Lomo dañado", r.getMotivo());
    }

    @Test
    void finalizarGuardaLaFechaDeHabilitacion() {
        RegistroMantenimiento r = new RegistroMantenimiento(1L, "Lomo dañado", INGRESO);
        LocalDateTime fecha = INGRESO.plusDays(2);
        r.finalizar(fecha);
        assertFalse(r.estaAbierto());
        assertEquals(fecha, r.getHabilitacion());
    }

    @Test
    void noSePuedeFinalizarDosVeces() {
        RegistroMantenimiento r = new RegistroMantenimiento(1L, "Lomo dañado", INGRESO);
        r.finalizar(INGRESO.plusDays(1));
        assertThrows(IllegalStateException.class, () -> r.finalizar(INGRESO.plusDays(2)));
    }

    @Test
    void rechazaFechasYMotivosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new RegistroMantenimiento(1L, " ", INGRESO));
        assertThrows(IllegalArgumentException.class, () -> new RegistroMantenimiento(1L, "Motivo", null));
        RegistroMantenimiento r = new RegistroMantenimiento(1L, "Motivo", INGRESO);
        assertThrows(IllegalArgumentException.class, () -> r.finalizar(null));
        assertThrows(IllegalArgumentException.class, () -> r.finalizar(INGRESO.minusHours(1)));
        assertTrue(r.estaAbierto());
    }
}
