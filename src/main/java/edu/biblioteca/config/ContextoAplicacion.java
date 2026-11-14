package edu.biblioteca.config;

import edu.biblioteca.repository.UsuarioSistemaRepository;
import edu.biblioteca.repository.jdbc.JdbcEjemplarRepository;
import edu.biblioteca.repository.jdbc.JdbcLectorRepository;
import edu.biblioteca.repository.jdbc.JdbcLibroRepository;
import edu.biblioteca.repository.jdbc.JdbcMantenimientoRepository;
import edu.biblioteca.repository.jdbc.JdbcPrestamoRepository;
import edu.biblioteca.repository.jdbc.JdbcUsuarioSistemaRepository;
import edu.biblioteca.security.AutenticacionService;
import edu.biblioteca.service.CatalogoService;
import edu.biblioteca.service.DashboardService;
import edu.biblioteca.service.LectorService;
import edu.biblioteca.service.MantenimientoService;
import edu.biblioteca.service.PrestamoService;

import java.time.Clock;
import java.util.Objects;

/**
 * Contenedor de inversión de control (Composition Root) que administra la base de datos,
 * inicialización DDL de esquemas, repositorios JDBC y servicios de dominio.
 */
public class ContextoAplicacion {

    private final Database db;
    private final JdbcUsuarioSistemaRepository usuarioRepo;
    private final CatalogoService catalogoService;
    private final MantenimientoService mantenimientoService;
    private final PrestamoService prestamoService;
    private final LectorService lectorService;
    private final AutenticacionService autenticacionService;
    private final DashboardService dashboardService;

    /**
     * Inicializa el backend para entorno de producción en el archivo biblioteca.db.
     */
    public static ContextoAplicacion inicializarProduccion() {
        Database db = new Database("biblioteca.db");
        db.inicializarEsquema("/db/schema-catalogo.sql", "/db/schema-seguridad.sql", "/db/schema-prestamos.sql");
        return new ContextoAplicacion(db);
    }

    /**
     * Inicializa el backend sobre una base de datos ya conectada (útil para pruebas de integración).
     */
    public static ContextoAplicacion inicializar(Database db) {
        return new ContextoAplicacion(db);
    }

    public ContextoAplicacion(Database db) {
        this.db = Objects.requireNonNull(db, "Database no puede ser nulo");

        JdbcLibroRepository libroRepo = new JdbcLibroRepository(db);
        JdbcEjemplarRepository ejemplarRepo = new JdbcEjemplarRepository(db);
        JdbcMantenimientoRepository mantenimientoRepo = new JdbcMantenimientoRepository(db);
        JdbcLectorRepository lectorRepo = new JdbcLectorRepository(db);
        JdbcPrestamoRepository prestamoRepo = new JdbcPrestamoRepository(db);
        this.usuarioRepo = new JdbcUsuarioSistemaRepository(db);

        Clock reloj = Clock.systemDefaultZone();
        this.catalogoService = new CatalogoService(libroRepo, ejemplarRepo);
        this.mantenimientoService = new MantenimientoService(db, ejemplarRepo, mantenimientoRepo);
        this.prestamoService = new PrestamoService(db, lectorRepo, ejemplarRepo, prestamoRepo, reloj);
        this.lectorService = new LectorService(db, lectorRepo, prestamoRepo);
        this.autenticacionService = new AutenticacionService(usuarioRepo);
        this.dashboardService = new DashboardService(libroRepo, ejemplarRepo, prestamoService);

        sembrarAdministradorInicialSiNoExiste();
    }

    private void sembrarAdministradorInicialSiNoExiste() {
        if (!usuarioRepo.existeAlgunaCuenta()) {
            autenticacionService.crearAdministradorInicial("admin", "Admin123*".toCharArray());
        }
    }

    public Database getDatabase() { return db; }
    public UsuarioSistemaRepository getUsuarioRepo() { return usuarioRepo; }
    public CatalogoService getCatalogoService() { return catalogoService; }
    public MantenimientoService getMantenimientoService() { return mantenimientoService; }
    public PrestamoService getPrestamoService() { return prestamoService; }
    public LectorService getLectorService() { return lectorService; }
    public AutenticacionService getAutenticacionService() { return autenticacionService; }
    public DashboardService getDashboardService() { return dashboardService; }
}
