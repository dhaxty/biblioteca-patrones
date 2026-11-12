package edu.biblioteca.service;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.Libro;
import edu.biblioteca.model.RegistroMantenimiento;
import edu.biblioteca.model.estado.TipoEstado;
import edu.biblioteca.repository.EjemplarRepository;
import edu.biblioteca.repository.LibroRepository;
import edu.biblioteca.repository.MantenimientoRepository;
import edu.biblioteca.repository.jdbc.JdbcEjemplarRepository;
import edu.biblioteca.repository.jdbc.JdbcLibroRepository;
import edu.biblioteca.repository.jdbc.JdbcMantenimientoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba de integración contra SQLite en memoria: repositorios JDBC + servicios del módulo A. */
class CatalogoMantenimientoIntegracionTest {

    private Database db;
    private LibroRepository libros;
    private EjemplarRepository ejemplares;
    private MantenimientoRepository mantenimientos;
    private CatalogoService catalogo;
    private MantenimientoService mantenimiento;

    @BeforeEach
    void preparar() {
        db = Database.enMemoria();
        db.inicializarEsquema("/db/schema-catalogo.sql");
        libros = new JdbcLibroRepository(db);
        ejemplares = new JdbcEjemplarRepository(db);
        mantenimientos = new JdbcMantenimientoRepository(db);
        catalogo = new CatalogoService(libros, ejemplares);
        mantenimiento = new MantenimientoService(db, ejemplares, mantenimientos);
    }

    @AfterEach
    void cerrar() {
        db.close();
    }

    private Libro libroDePrueba() {
        return catalogo.registrarLibro("978-0-13-468599-1", "Effective Java", "Joshua Bloch",
                "Addison-Wesley", 2018, "Programación");
    }

    @Test
    void registraYBuscaLibros() {
        Libro libro = libroDePrueba();
        assertTrue(libro.getId() != null && libro.getId() > 0);
        catalogo.agregarEjemplar(libro.getId(), "EJ-0001");

        assertEquals(1, catalogo.buscarLibros("effective").size());
        assertEquals(1, catalogo.buscarLibros("bloch").size());
        assertEquals(1, catalogo.buscarLibros("EJ-0001").size());
        assertEquals(1, catalogo.buscarLibros("").size());
        assertEquals(0, catalogo.buscarLibros("python").size());
    }

    @Test
    void rechazaIsbnYCodigoDuplicados() {
        Libro libro = libroDePrueba();
        assertThrows(ReglaNegocioException.class, this::libroDePrueba);

        catalogo.agregarEjemplar(libro.getId(), "EJ-0001");
        assertThrows(ReglaNegocioException.class, () -> catalogo.agregarEjemplar(libro.getId(), "EJ-0001"));
    }

    @Test
    void validaDatosDelLibro() {
        assertThrows(ReglaNegocioException.class,
                () -> catalogo.registrarLibro("111", " ", "Autor", "Ed", 2020, "X"));
        assertThrows(ReglaNegocioException.class,
                () -> catalogo.registrarLibro("111", "Titulo", "Autor", "Ed", 3000, "X"));
    }

    @Test
    void desactivacionLogicaConservaElLibro() {
        Libro libro = libroDePrueba();
        catalogo.desactivarLibro(libro.getId());

        assertEquals(0, catalogo.buscarLibros("effective").size());
        assertFalse(libros.buscarPorId(libro.getId()).orElseThrow().isActivo());
        assertThrows(ReglaNegocioException.class, () -> catalogo.agregarEjemplar(libro.getId(), "EJ-0002"));
    }

    @Test
    void noDesactivaLibroConEjemplarPrestado() {
        Libro libro = libroDePrueba();
        Ejemplar ejemplar = catalogo.agregarEjemplar(libro.getId(), "EJ-0001");
        ejemplar.prestar();
        ejemplares.actualizar(ejemplar);

        assertThrows(ReglaNegocioException.class, () -> catalogo.desactivarLibro(libro.getId()));
    }

    @Test
    void cicloRetiroYHabilitacion() {
        Libro libro = libroDePrueba();
        Ejemplar ejemplar = catalogo.agregarEjemplar(libro.getId(), "EJ-0001");

        RegistroMantenimiento abierto = mantenimiento.retirarEjemplar(ejemplar.getId(), "Lomo despegado");
        assertTrue(abierto.estaAbierto());
        assertEquals(TipoEstado.EN_MANTENIMIENTO,
                ejemplares.buscarPorId(ejemplar.getId()).orElseThrow().getTipoEstado());
        assertEquals(1, mantenimiento.listarEjemplaresEnMantenimiento().size());

        RegistroMantenimiento cerrado = mantenimiento.habilitarEjemplar(ejemplar.getId());
        assertFalse(cerrado.estaAbierto());
        assertEquals(TipoEstado.DISPONIBLE,
                ejemplares.buscarPorId(ejemplar.getId()).orElseThrow().getTipoEstado());

        List<RegistroMantenimiento> historial = mantenimiento.consultarHistorial(ejemplar.getId());
        assertEquals(1, historial.size());
        assertFalse(historial.get(0).estaAbierto());
    }

    @Test
    void retirarRequiereMotivoYEstadoValido() {
        Libro libro = libroDePrueba();
        Ejemplar ejemplar = catalogo.agregarEjemplar(libro.getId(), "EJ-0001");

        assertThrows(ReglaNegocioException.class, () -> mantenimiento.retirarEjemplar(ejemplar.getId(), " "));

        ejemplar.prestar();
        ejemplares.actualizar(ejemplar);
        assertThrows(ReglaNegocioException.class, () -> mantenimiento.retirarEjemplar(ejemplar.getId(), "Daño"));
        assertThrows(ReglaNegocioException.class, () -> mantenimiento.habilitarEjemplar(ejemplar.getId()));
    }

    @Test
    void devolucionDanadaDejaRegistroDeMantenimiento() {
        Libro libro = libroDePrueba();
        Ejemplar ejemplar = catalogo.agregarEjemplar(libro.getId(), "EJ-0001");
        ejemplar.prestar();
        ejemplares.actualizar(ejemplar);

        // Lo que hará el Facade del módulo B, dentro de su propia transacción:
        db.enTransaccion(() -> {
            ejemplar.devolverConDanio();
            ejemplares.actualizar(ejemplar);
            mantenimiento.registrarIngresoPorDanio(ejemplar.getId(), "Hojas mojadas");
        });

        assertEquals(TipoEstado.EN_MANTENIMIENTO,
                ejemplares.buscarPorId(ejemplar.getId()).orElseThrow().getTipoEstado());
        assertTrue(mantenimientos.buscarAbiertoPorEjemplar(ejemplar.getId()).isPresent());
    }

    @Test
    void laTransaccionHaceRollbackSiAlgoFalla() {
        Libro libro = libroDePrueba();
        Ejemplar ejemplar = catalogo.agregarEjemplar(libro.getId(), "EJ-0001");

        assertThrows(IllegalStateException.class, () -> db.enTransaccion(() -> {
            ejemplar.enviarAMantenimiento();
            ejemplares.actualizar(ejemplar);
            throw new IllegalStateException("falla simulada");
        }));

        assertEquals(TipoEstado.DISPONIBLE,
                ejemplares.buscarPorId(ejemplar.getId()).orElseThrow().getTipoEstado());
    }

    @Test
    void cuentaParaElDashboard() {
        Libro libro = libroDePrueba();
        catalogo.agregarEjemplar(libro.getId(), "EJ-0001");
        Ejemplar segundo = catalogo.agregarEjemplar(libro.getId(), "EJ-0002");
        mantenimiento.retirarEjemplar(segundo.getId(), "Revisión");

        assertEquals(1, libros.contarActivos());
        assertEquals(1, ejemplares.contarPorEstado(TipoEstado.DISPONIBLE));
        assertEquals(1, ejemplares.contarPorEstado(TipoEstado.EN_MANTENIMIENTO));
    }
}
