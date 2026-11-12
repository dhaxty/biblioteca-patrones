package biblioteca.service;

import biblioteca.BaseDatosDePrueba;
import biblioteca.model.Ejemplar;
import biblioteca.model.RegistroMantenimiento;
import biblioteca.repository.MantenimientoRepository;
import biblioteca.repository.RepositoryException;
import biblioteca.repository.jdbc.EjemplarRepositoryJdbc;
import biblioteca.repository.jdbc.LibroRepositoryJdbc;
import biblioteca.repository.jdbc.MantenimientoRepositoryJdbc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MantenimientoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("UTC");

    private Connection connection;
    private EjemplarRepositoryJdbc ejemplares;
    private MantenimientoRepositoryJdbc mantenimientos;
    private MantenimientoService servicio;
    private Ejemplar ejemplar;

    @BeforeEach
    void preparar() throws SQLException {
        connection = BaseDatosDePrueba.nueva();
        ejemplares = new EjemplarRepositoryJdbc(connection);
        mantenimientos = new MantenimientoRepositoryJdbc(connection);
        servicio = new MantenimientoService(connection, ejemplares, mantenimientos,
                Clock.fixed(Instant.parse("2026-05-01T10:00:00Z"), ZONA));

        var libro = new LibroRepositoryJdbc(connection).guardar(BaseDatosDePrueba.libroEjemplo("9780306406157"));
        ejemplar = ejemplares.guardar(new Ejemplar("EJ-001", libro));
    }

    @AfterEach
    void cerrar() throws SQLException {
        connection.close();
    }

    private String estadoPersistido() {
        return ejemplares.buscarPorId(ejemplar.getId()).orElseThrow().getEstadoNombre();
    }

    @Test
    void enviarAMantenimientoCambiaElEstadoYCreaElRegistro() throws SQLException {
        RegistroMantenimiento r = servicio.enviarAMantenimiento(ejemplar.getId(), "Tapa rota");

        assertNotNull(r.getId());
        assertTrue(r.estaAbierto());
        assertEquals(LocalDateTime.of(2026, 5, 1, 10, 0), r.getIngreso());
        assertEquals("EN_MANTENIMIENTO", estadoPersistido());
        assertTrue(connection.getAutoCommit(), "debe restaurar el autocommit");
    }

    @Test
    void habilitarCierraElRegistroYDejaElEjemplarDisponible() {
        servicio.enviarAMantenimiento(ejemplar.getId(), "Tapa rota");
        RegistroMantenimiento r = servicio.habilitar(ejemplar.getId());

        assertFalse(r.estaAbierto());
        assertEquals(LocalDateTime.of(2026, 5, 1, 10, 0), r.getHabilitacion());
        assertEquals("DISPONIBLE", estadoPersistido());
        assertTrue(mantenimientos.buscarAbiertoPorEjemplar(ejemplar.getId()).isEmpty());
    }

    @Test
    void historialAcumulaVariosMantenimientos() {
        servicio.enviarAMantenimiento(ejemplar.getId(), "Primero");
        servicio.habilitar(ejemplar.getId());
        servicio.enviarAMantenimiento(ejemplar.getId(), "Segundo");

        List<RegistroMantenimiento> historial = servicio.historial(ejemplar.getId());
        assertEquals(2, historial.size());
        assertFalse(historial.get(0).estaAbierto());
        assertTrue(historial.get(1).estaAbierto());
    }

    @Test
    void ejemplarPrestadoNoPuedeEnviarseAMantenimiento() {
        ejemplar.prestar();
        ejemplares.actualizar(ejemplar);

        assertThrows(IllegalStateException.class,
                () -> servicio.enviarAMantenimiento(ejemplar.getId(), "Motivo"));
        assertEquals("PRESTADO", estadoPersistido());
        assertTrue(servicio.historial(ejemplar.getId()).isEmpty());
    }

    @Test
    void noSePuedeEnviarDosVecesAMantenimiento() {
        servicio.enviarAMantenimiento(ejemplar.getId(), "Motivo");
        assertThrows(IllegalStateException.class,
                () -> servicio.enviarAMantenimiento(ejemplar.getId(), "Otra vez"));
        assertEquals(1, servicio.historial(ejemplar.getId()).size());
    }

    @Test
    void habilitarUnEjemplarDisponibleLanzaIllegalState() {
        assertThrows(IllegalStateException.class, () -> servicio.habilitar(ejemplar.getId()));
        assertEquals("DISPONIBLE", estadoPersistido());
    }

    @Test
    void argumentosInvalidosLanzanIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> servicio.enviarAMantenimiento(ejemplar.getId(), " "));
        assertThrows(IllegalArgumentException.class, () -> servicio.enviarAMantenimiento(ejemplar.getId(), null));
        assertThrows(IllegalArgumentException.class, () -> servicio.enviarAMantenimiento(999, "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> servicio.habilitar(999));
    }

    @Test
    void siFallaElRegistroSeHaceRollbackDelCambioDeEstado() throws SQLException {
        MantenimientoRepository roto = new MantenimientoRepository() {
            @Override
            public RegistroMantenimiento guardar(RegistroMantenimiento registro) {
                throw new RepositoryException("fallo simulado", new SQLException("boom"));
            }

            @Override
            public void actualizar(RegistroMantenimiento registro) {
            }

            @Override
            public Optional<RegistroMantenimiento> buscarPorId(long id) {
                return Optional.empty();
            }

            @Override
            public Optional<RegistroMantenimiento> buscarAbiertoPorEjemplar(long ejemplarId) {
                return Optional.empty();
            }

            @Override
            public List<RegistroMantenimiento> listarPorEjemplar(long ejemplarId) {
                return List.of();
            }
        };
        MantenimientoService servicioRoto = new MantenimientoService(connection, ejemplares, roto);

        assertThrows(RepositoryException.class,
                () -> servicioRoto.enviarAMantenimiento(ejemplar.getId(), "Motivo"));

        assertEquals("DISPONIBLE", estadoPersistido(), "el UPDATE del ejemplar debe revertirse");
        assertTrue(connection.getAutoCommit(), "debe restaurar el autocommit tras el rollback");
    }

    @Test
    void habilitarSinRegistroAbiertoHaceRollback() throws SQLException {
        // Estado inconsistente: EN_MANTENIMIENTO pero sin registro abierto.
        ejemplar.enviarAMantenimiento();
        ejemplares.actualizar(ejemplar);

        assertThrows(IllegalStateException.class, () -> servicio.habilitar(ejemplar.getId()));
        assertEquals("EN_MANTENIMIENTO", estadoPersistido());
    }

    @Test
    void dependenciasNulasSonRechazadas() {
        assertThrows(IllegalArgumentException.class,
                () -> new MantenimientoService(null, ejemplares, mantenimientos));
        assertThrows(IllegalArgumentException.class,
                () -> new MantenimientoService(connection, null, mantenimientos));
    }
}
