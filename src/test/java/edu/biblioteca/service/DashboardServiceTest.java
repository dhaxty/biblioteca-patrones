package edu.biblioteca.service;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.TipoLector;
import edu.biblioteca.repository.EjemplarRepository;
import edu.biblioteca.repository.LectorRepository;
import edu.biblioteca.repository.LibroRepository;
import edu.biblioteca.repository.PrestamoRepository;
import edu.biblioteca.repository.jdbc.JdbcEjemplarRepository;
import edu.biblioteca.repository.jdbc.JdbcLectorRepository;
import edu.biblioteca.repository.jdbc.JdbcLibroRepository;
import edu.biblioteca.repository.jdbc.JdbcPrestamoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardServiceTest {

    private Database db;
    private LibroRepository libros;
    private EjemplarRepository ejemplares;
    private LectorRepository lectores;
    private PrestamoRepository prestamos;
    private CatalogoService catalogoService;
    private LectorService lectorService;
    private PrestamoService prestamoService;
    private DashboardService dashboardService;

    @BeforeEach
    void setup() {
        db = Database.enMemoria();
        db.inicializarEsquema("/db/schema-catalogo.sql", "/db/schema-seguridad.sql", "/db/schema-prestamos.sql");

        libros = new JdbcLibroRepository(db);
        ejemplares = new JdbcEjemplarRepository(db);
        lectores = new JdbcLectorRepository(db);
        prestamos = new JdbcPrestamoRepository(db);

        Clock reloj = Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneId.of("UTC"));
        catalogoService = new CatalogoService(libros, ejemplares);
        lectorService = new LectorService(db, lectores, prestamos);
        prestamoService = new PrestamoService(db, lectores, ejemplares, prestamos, reloj);
        dashboardService = new DashboardService(libros, ejemplares, prestamoService);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void obtenerIndicadoresConsolidadosCorrectamente() {
        // Inicialmente todo en cero
        Indicadores iniciales = dashboardService.obtenerIndicadores();
        assertEquals(0, iniciales.totalLibros());
        assertEquals(0, iniciales.ejemplaresDisponibles());
        assertEquals(0, iniciales.prestamosActivos());
        assertEquals(0, iniciales.prestamosVencidos());

        // Registrar libro y ejemplares
        var libro = catalogoService.registrarLibro("978-0-13-235088-4", "Clean Code", "Robert Martin", "Prentice", 2008, "Software");
        catalogoService.agregarEjemplar(libro.getId(), "EJ-001");
        catalogoService.agregarEjemplar(libro.getId(), "EJ-002");

        // Registrar lector y préstamo
        lectorService.registrarLector("U12345678", "Juan Perez", "juan@utp.edu.pe", TipoLector.ESTUDIANTE);
        prestamoService.prestar("U12345678", "EJ-001", 1L);

        Indicadores actuales = dashboardService.obtenerIndicadores();
        assertEquals(1, actuales.totalLibros());
        assertEquals(1, actuales.ejemplaresDisponibles());
        assertEquals(1, actuales.prestamosActivos());
        assertEquals(0, actuales.prestamosVencidos());
    }
}
