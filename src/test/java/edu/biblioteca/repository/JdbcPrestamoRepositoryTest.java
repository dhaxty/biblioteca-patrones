package edu.biblioteca.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Lector;
import edu.biblioteca.model.Prestamo;
import edu.biblioteca.model.PrestamoDetalle;
import edu.biblioteca.model.TipoLector;
import edu.biblioteca.repository.jdbc.JdbcLectorRepository;
import edu.biblioteca.repository.jdbc.JdbcPrestamoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/** Integración con SQLite en memoria y el esquema real (catálogo + préstamos). */
class JdbcPrestamoRepositoryTest {

    private Database db;
    private JdbcLectorRepository lectores;
    private JdbcPrestamoRepository prestamos;
    private long lectorId;

    @BeforeEach
    void preparar() throws SQLException {
        db = Database.enMemoria();
        db.inicializarEsquema("/db/schema-catalogo.sql", "/db/schema-prestamos.sql");
        try (Statement st = db.conexion().createStatement()) {
            st.execute("INSERT INTO libro (isbn, titulo, autor, anio) VALUES ('111', 'Patrones de Diseño', 'GoF', 1994)");
            st.execute("INSERT INTO ejemplar (codigo, libro_id) VALUES ('EJ-1', 1), ('EJ-2', 1)");
        }
        lectores = new JdbcLectorRepository(db);
        prestamos = new JdbcPrestamoRepository(db);
        lectorId = lectores.guardar(new Lector("U001", "Ana Torres", "ana@uni.edu", TipoLector.ESTUDIANTE)).getId();
    }

    @AfterEach
    void cerrar() {
        db.close();
    }

    @Test
    void lectorSeGuardaYSeBuscaPorCodigoYTexto() {
        assertEquals("Ana Torres", lectores.buscarPorCodigo("U001").orElseThrow().getNombre());
        assertEquals(1, lectores.buscar("torres").size());
        assertTrue(lectores.buscarPorCodigo("NOPE").isEmpty());
    }

    @Test
    void listaActivosYVencidosConTituloYCodigoDeEjemplar() {
        prestamos.guardar(Prestamo.nuevo(lectorId, 1, 1, LocalDate.of(2026, 11, 2)));
        prestamos.guardar(Prestamo.nuevo(lectorId, 2, 1, LocalDate.of(2026, 11, 10)));
        LocalDate hoy = LocalDate.of(2026, 11, 12);

        List<PrestamoDetalle> activos = prestamos.listarActivos(hoy);
        List<PrestamoDetalle> vencidos = prestamos.listarVencidos(hoy);

        assertEquals(2, activos.size());
        assertEquals("Patrones de Diseño", activos.get(0).tituloLibro());
        assertEquals("EJ-1", activos.get(0).codigoEjemplar());
        assertEquals(1, vencidos.size());
        assertTrue(vencidos.get(0).vencido());
        assertEquals(2L, prestamos.contarActivos());
        assertEquals(1L, prestamos.contarVencidos(hoy));
        assertTrue(prestamos.existeVencidoPorLector(lectorId, hoy));
    }

    @Test
    void cerrarSacaElPrestamoDeActivos() {
        Prestamo p = prestamos.guardar(Prestamo.nuevo(lectorId, 1, 1, LocalDate.of(2026, 11, 2)));
        p.cerrar(LocalDate.of(2026, 11, 5));
        prestamos.cerrar(p);

        assertEquals(0L, prestamos.contarActivosPorLector(lectorId));
        assertFalse(prestamos.buscarActivoPorEjemplar(1).isPresent());
    }

    @Test
    void unEjemplarNoPuedeTenerDosPrestamosActivos() {
        prestamos.guardar(Prestamo.nuevo(lectorId, 1, 1, LocalDate.of(2026, 11, 2)));
        assertThrows(RepositorioException.class,
                () -> prestamos.guardar(Prestamo.nuevo(lectorId, 1, 1, LocalDate.of(2026, 11, 3))));
    }

    @Test
    void siLaTransaccionFallaNoQuedaNingunPrestamo() {
        assertThrows(IllegalStateException.class, () -> db.enTransaccion(() -> {
            prestamos.guardar(Prestamo.nuevo(lectorId, 1, 1, LocalDate.of(2026, 11, 2)));
            throw new IllegalStateException("falla a mitad de la operación");
        }));
        assertEquals(0L, prestamos.contarActivos());
    }
}
