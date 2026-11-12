package biblioteca.repository.jdbc;

import biblioteca.BaseDatosDePrueba;
import biblioteca.model.Ejemplar;
import biblioteca.model.Libro;
import biblioteca.repository.RepositoryException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EjemplarRepositoryJdbcTest {

    private Connection connection;
    private LibroRepositoryJdbc libros;
    private EjemplarRepositoryJdbc ejemplares;
    private Libro libro;

    @BeforeEach
    void preparar() throws SQLException {
        connection = BaseDatosDePrueba.nueva();
        libros = new LibroRepositoryJdbc(connection);
        ejemplares = new EjemplarRepositoryJdbc(connection);
        libro = libros.guardar(BaseDatosDePrueba.libroEjemplo("9780306406157"));
    }

    @AfterEach
    void cerrar() throws SQLException {
        connection.close();
    }

    @Test
    void guardaYRecuperaConSuLibro() {
        Ejemplar guardado = ejemplares.guardar(new Ejemplar("EJ-001", libro));
        assertNotNull(guardado.getId());

        Ejemplar leido = ejemplares.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("EJ-001", leido.getCodigoUnico());
        assertEquals("DISPONIBLE", leido.getEstadoNombre());
        assertEquals(libro.getId(), leido.getLibro().getId());
        assertEquals("Cien años de soledad", leido.getLibro().getTitulo());
        assertTrue(ejemplares.buscarPorCodigo("EJ-001").isPresent());
        assertTrue(ejemplares.buscarPorCodigo("NO-EXISTE").isEmpty());
    }

    @Test
    void persisteLasTransicionesDeEstado() {
        Ejemplar e = ejemplares.guardar(new Ejemplar("EJ-001", libro));
        e.prestar();
        ejemplares.actualizar(e);
        assertEquals("PRESTADO", ejemplares.buscarPorId(e.getId()).orElseThrow().getEstadoNombre());

        e.devolver();
        e.enviarAMantenimiento();
        ejemplares.actualizar(e);
        assertEquals("EN_MANTENIMIENTO", ejemplares.buscarPorId(e.getId()).orElseThrow().getEstadoNombre());
    }

    @Test
    void codigoDuplicadoLanzaRepositoryException() {
        ejemplares.guardar(new Ejemplar("EJ-001", libro));
        assertThrows(RepositoryException.class, () -> ejemplares.guardar(new Ejemplar("EJ-001", libro)));
    }

    @Test
    void llaveForaneaImpideEjemplarDeLibroInexistente() {
        Libro fantasma = new Libro(9999L, "9781402894626", "T", "A", "E", 2000, "C", true);
        assertThrows(RepositoryException.class, () -> ejemplares.guardar(new Ejemplar("EJ-002", fantasma)));
    }

    @Test
    void guardarConLibroSinIdLanzaIllegalArgument() {
        Libro sinId = new Libro("9781402894626", "T", "A", "E", 2000, "C");
        assertThrows(IllegalArgumentException.class, () -> ejemplares.guardar(new Ejemplar("EJ-003", sinId)));
    }

    @Test
    void actualizarInexistenteLanzaIllegalArgument() {
        Ejemplar e = new Ejemplar(777L, "EJ-X", libro, new biblioteca.model.estado.EstadoDisponible());
        assertThrows(IllegalArgumentException.class, () -> ejemplares.actualizar(e));
    }

    @Test
    void listaPorLibroYTodos() {
        Libro otro = libros.guardar(new Libro("9781402894626", "Otro", "A", "E", 2000, "C"));
        ejemplares.guardar(new Ejemplar("EJ-001", libro));
        ejemplares.guardar(new Ejemplar("EJ-002", libro));
        ejemplares.guardar(new Ejemplar("EJ-003", otro));

        assertEquals(2, ejemplares.buscarPorLibro(libro.getId()).size());
        assertEquals(1, ejemplares.buscarPorLibro(otro.getId()).size());
        assertEquals(3, ejemplares.listarTodos().size());
    }
}
