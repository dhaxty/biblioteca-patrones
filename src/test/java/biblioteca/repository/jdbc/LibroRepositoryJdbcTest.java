package biblioteca.repository.jdbc;

import biblioteca.BaseDatosDePrueba;
import biblioteca.model.Libro;
import biblioteca.repository.RepositoryException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibroRepositoryJdbcTest {

    private Connection connection;
    private LibroRepositoryJdbc repo;

    @BeforeEach
    void preparar() throws SQLException {
        connection = BaseDatosDePrueba.nueva();
        repo = new LibroRepositoryJdbc(connection);
    }

    @AfterEach
    void cerrar() throws SQLException {
        connection.close();
    }

    @Test
    void guardaYRecuperaPorIdEIsbn() {
        Libro guardado = repo.guardar(BaseDatosDePrueba.libroEjemplo("9780306406157"));
        assertNotNull(guardado.getId());

        Libro porId = repo.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("Cien años de soledad", porId.getTitulo());
        assertTrue(porId.isActivo());
        assertTrue(repo.buscarPorIsbn("9780306406157").isPresent());
        assertTrue(repo.buscarPorIsbn("9999999999999").isEmpty());
        assertTrue(repo.buscarPorId(999).isEmpty());
    }

    @Test
    void isbnDuplicadoLanzaRepositoryException() {
        repo.guardar(BaseDatosDePrueba.libroEjemplo("9780306406157"));
        assertThrows(RepositoryException.class,
                () -> repo.guardar(BaseDatosDePrueba.libroEjemplo("9780306406157")));
    }

    @Test
    void actualizaDatos() {
        Libro libro = repo.guardar(BaseDatosDePrueba.libroEjemplo("9780306406157"));
        libro.actualizar("Otro título", "Otro autor", "Otra editorial", 2001, "Ensayo");
        repo.actualizar(libro);

        Libro leido = repo.buscarPorId(libro.getId()).orElseThrow();
        assertEquals("Otro título", leido.getTitulo());
        assertEquals(2001, leido.getAnio());
    }

    @Test
    void actualizarLibroInexistenteLanzaIllegalArgument() {
        Libro sinGuardar = BaseDatosDePrueba.libroEjemplo("9780306406157");
        assertThrows(IllegalArgumentException.class, () -> repo.actualizar(sinGuardar));
        sinGuardar.setId(12345L);
        assertThrows(IllegalArgumentException.class, () -> repo.actualizar(sinGuardar));
    }

    @Test
    void desactivarEsBorradoLogicoYNoEliminaLaFila() throws SQLException {
        Libro libro = repo.guardar(BaseDatosDePrueba.libroEjemplo("9780306406157"));
        repo.desactivar(libro.getId());

        assertFalse(repo.buscarPorId(libro.getId()).orElseThrow().isActivo());
        assertTrue(repo.listarActivos().isEmpty());
        assertEquals(1, repo.listarTodos().size());

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT activo FROM libro WHERE id = " + libro.getId())) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt("activo"));
        }
    }

    @Test
    void desactivarInexistenteLanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> repo.desactivar(999));
    }

    @Test
    void listaSoloActivosYTodos() {
        Libro a = repo.guardar(BaseDatosDePrueba.libroEjemplo("9780306406157"));
        repo.guardar(new Libro("9781402894626", "Otro", "Autor", "Ed", 2005, "Cat"));
        repo.desactivar(a.getId());

        assertEquals(2, repo.listarTodos().size());
        assertEquals(1, repo.listarActivos().size());
        assertEquals("Otro", repo.listarActivos().get(0).getTitulo());
    }

    @Test
    void conexionNulaEsRechazada() {
        assertThrows(IllegalArgumentException.class, () -> new LibroRepositoryJdbc(null));
    }
}
