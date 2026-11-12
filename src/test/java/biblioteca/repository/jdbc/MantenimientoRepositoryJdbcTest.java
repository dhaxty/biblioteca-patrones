package biblioteca.repository.jdbc;

import biblioteca.BaseDatosDePrueba;
import biblioteca.model.Ejemplar;
import biblioteca.model.RegistroMantenimiento;
import biblioteca.repository.RepositoryException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MantenimientoRepositoryJdbcTest {

    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 3, 1, 8, 30, 15);

    private Connection connection;
    private MantenimientoRepositoryJdbc repo;
    private Ejemplar ejemplar;

    @BeforeEach
    void preparar() throws SQLException {
        connection = BaseDatosDePrueba.nueva();
        repo = new MantenimientoRepositoryJdbc(connection);
        var libro = new LibroRepositoryJdbc(connection).guardar(BaseDatosDePrueba.libroEjemplo("9780306406157"));
        ejemplar = new EjemplarRepositoryJdbc(connection).guardar(new Ejemplar("EJ-001", libro));
    }

    @AfterEach
    void cerrar() throws SQLException {
        connection.close();
    }

    @Test
    void guardaYRecuperaConFechasIso() {
        RegistroMantenimiento guardado = repo.guardar(new RegistroMantenimiento(ejemplar.getId(), "Hojas sueltas", INGRESO));
        assertNotNull(guardado.getId());

        RegistroMantenimiento leido = repo.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals(INGRESO, leido.getIngreso());
        assertNull(leido.getHabilitacion());
        assertEquals("Hojas sueltas", leido.getMotivo());
    }

    @Test
    void actualizaLaFechaDeHabilitacion() {
        RegistroMantenimiento r = repo.guardar(new RegistroMantenimiento(ejemplar.getId(), "Hojas sueltas", INGRESO));
        LocalDateTime fecha = INGRESO.plusDays(3);
        r.finalizar(fecha);
        repo.actualizar(r);

        assertEquals(fecha, repo.buscarPorId(r.getId()).orElseThrow().getHabilitacion());
    }

    @Test
    void buscaElRegistroAbiertoYListaElHistorial() {
        RegistroMantenimiento primero = repo.guardar(new RegistroMantenimiento(ejemplar.getId(), "Primero", INGRESO));
        primero.finalizar(INGRESO.plusDays(1));
        repo.actualizar(primero);
        assertTrue(repo.buscarAbiertoPorEjemplar(ejemplar.getId()).isEmpty());

        RegistroMantenimiento segundo = repo.guardar(new RegistroMantenimiento(ejemplar.getId(), "Segundo", INGRESO.plusDays(5)));
        assertEquals(segundo.getId(), repo.buscarAbiertoPorEjemplar(ejemplar.getId()).orElseThrow().getId());

        var historial = repo.listarPorEjemplar(ejemplar.getId());
        assertEquals(2, historial.size());
        assertEquals("Primero", historial.get(0).getMotivo());
        assertEquals("Segundo", historial.get(1).getMotivo());
    }

    @Test
    void llaveForaneaImpideRegistroDeEjemplarInexistente() {
        assertThrows(RepositoryException.class,
                () -> repo.guardar(new RegistroMantenimiento(9999L, "Motivo", INGRESO)));
    }

    @Test
    void actualizarInexistenteLanzaIllegalArgument() {
        RegistroMantenimiento r = new RegistroMantenimiento(555L, ejemplar.getId(), "M", INGRESO, null);
        assertThrows(IllegalArgumentException.class, () -> repo.actualizar(r));
    }
}
