package biblioteca.service;

import biblioteca.BaseDatosDePrueba;
import biblioteca.model.Ejemplar;
import biblioteca.model.Libro;
import biblioteca.repository.jdbc.EjemplarRepositoryJdbc;
import biblioteca.repository.jdbc.LibroRepositoryJdbc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogoServiceTest {

    private Connection connection;
    private EjemplarRepositoryJdbc ejemplares;
    private CatalogoService servicio;

    @BeforeEach
    void preparar() throws SQLException {
        connection = BaseDatosDePrueba.nueva();
        ejemplares = new EjemplarRepositoryJdbc(connection);
        servicio = new CatalogoService(new LibroRepositoryJdbc(connection), ejemplares);
    }

    @AfterEach
    void cerrar() throws SQLException {
        connection.close();
    }

    private Libro registrarLibro() {
        return servicio.registrarLibro("978-0-306-40615-7", "Título", "Autor", "Editorial", 2000, "Ciencia");
    }

    @Test
    void registraLibroConIdYIsbnNormalizado() {
        Libro libro = registrarLibro();
        assertNotNull(libro.getId());
        assertEquals("9780306406157", libro.getIsbn());
        assertTrue(servicio.buscarLibroPorIsbn("9780306406157").isPresent());
    }

    @Test
    void isbnDuplicadoLanzaIllegalArgument() {
        registrarLibro();
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarLibro("9780306406157", "Otro", "A", "E", 2001, "C"));
    }

    @Test
    void datosInvalidosLanzanIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarLibro("123", "T", "A", "E", 2000, "C"));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarLibro("9780306406157", "", "A", "E", 2000, "C"));
    }

    @Test
    void actualizaLibro() {
        Libro libro = registrarLibro();
        servicio.actualizarLibro(libro.getId(), "Nuevo", "Autor2", "Ed2", 2010, "Arte");
        assertEquals("Nuevo", servicio.obtenerLibro(libro.getId()).getTitulo());
    }

    @Test
    void actualizarLibroInexistenteLanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarLibro(999, "T", "A", "E", 2000, "C"));
    }

    @Test
    void desactivarLibroEsLogico() {
        Libro libro = registrarLibro();
        servicio.desactivarLibro(libro.getId());

        assertFalse(servicio.obtenerLibro(libro.getId()).isActivo());
        assertTrue(servicio.listarLibrosActivos().isEmpty());
        assertEquals(1, servicio.listarLibros().size());
    }

    @Test
    void desactivarDosVecesEsIdempotente() {
        Libro libro = registrarLibro();
        servicio.desactivarLibro(libro.getId());
        servicio.desactivarLibro(libro.getId());
        assertFalse(servicio.obtenerLibro(libro.getId()).isActivo());
    }

    @Test
    void noSeDesactivaUnLibroConEjemplaresPrestados() {
        Libro libro = registrarLibro();
        Ejemplar e = servicio.registrarEjemplar("EJ-001", libro.getId());
        e.prestar();
        ejemplares.actualizar(e);

        assertThrows(IllegalStateException.class, () -> servicio.desactivarLibro(libro.getId()));
        assertTrue(servicio.obtenerLibro(libro.getId()).isActivo());
    }

    @Test
    void registraEjemplarDisponible() {
        Libro libro = registrarLibro();
        Ejemplar e = servicio.registrarEjemplar(" EJ-001 ", libro.getId());

        assertNotNull(e.getId());
        assertEquals("EJ-001", e.getCodigoUnico());
        assertEquals("DISPONIBLE", e.getEstadoNombre());
        assertEquals(1, servicio.listarEjemplaresDeLibro(libro.getId()).size());
        assertTrue(servicio.buscarEjemplarPorCodigo("EJ-001").isPresent());
    }

    @Test
    void codigoDeEjemplarDuplicadoLanzaIllegalArgument() {
        Libro libro = registrarLibro();
        servicio.registrarEjemplar("EJ-001", libro.getId());
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEjemplar("EJ-001", libro.getId()));
    }

    @Test
    void ejemplarConDatosInvalidosOLibroInexistente() {
        Libro libro = registrarLibro();
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEjemplar(" ", libro.getId()));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEjemplar("EJ-002", 999));
    }

    @Test
    void noSeRegistranEjemplaresDeUnLibroInactivo() {
        Libro libro = registrarLibro();
        servicio.desactivarLibro(libro.getId());
        assertThrows(IllegalStateException.class, () -> servicio.registrarEjemplar("EJ-001", libro.getId()));
    }

    @Test
    void obtenerEjemplarInexistenteLanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> servicio.obtenerEjemplar(999));
    }
}
