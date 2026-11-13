package edu.biblioteca;

import edu.biblioteca.config.Database;
import edu.biblioteca.controller.DashboardController;
import edu.biblioteca.controller.LectorController;
import edu.biblioteca.controller.LibroController;
import edu.biblioteca.controller.LoginController;
import edu.biblioteca.controller.MantenimientoController;
import edu.biblioteca.controller.PersonalController;
import edu.biblioteca.controller.PrestamoController;
import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.repository.jdbc.JdbcEjemplarRepository;
import edu.biblioteca.repository.jdbc.JdbcLectorRepository;
import edu.biblioteca.repository.jdbc.JdbcLibroRepository;
import edu.biblioteca.repository.jdbc.JdbcMantenimientoRepository;
import edu.biblioteca.repository.jdbc.JdbcPrestamoRepository;
import edu.biblioteca.repository.jdbc.JdbcUsuarioSistemaRepository;
import edu.biblioteca.security.AutenticacionService;
import edu.biblioteca.security.GestionCuentas;
import edu.biblioteca.security.GestionCuentasFactory;
import edu.biblioteca.security.Rol;
import edu.biblioteca.security.Sesion;
import edu.biblioteca.service.BibliotecaFacade;
import edu.biblioteca.service.CatalogoService;
import edu.biblioteca.service.DashboardService;
import edu.biblioteca.service.LectorService;
import edu.biblioteca.service.MantenimientoService;
import edu.biblioteca.service.PrestamoService;
import edu.biblioteca.view.LoginFrame;
import edu.biblioteca.view.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.Clock;

public class Main {

    private static Database db;
    private static JdbcLibroRepository libroRepo;
    private static JdbcEjemplarRepository ejemplarRepo;
    private static JdbcMantenimientoRepository mantenimientoRepo;
    private static JdbcLectorRepository lectorRepo;
    private static JdbcPrestamoRepository prestamoRepo;
    private static JdbcUsuarioSistemaRepository usuarioRepo;

    private static CatalogoService catalogoService;
    private static MantenimientoService mantenimientoService;
    private static PrestamoService prestamoService;
    private static LectorService lectorService;
    private static AutenticacionService autenticacionService;
    private static DashboardService dashboardService;

    public static void main(String[] args) {
        usarEstiloNimbus();
        inicializarBackend();
        SwingUtilities.invokeLater(Main::mostrarLogin);
    }

    private static void inicializarBackend() {
        db = new Database("biblioteca.db");
        db.inicializarEsquema("/db/schema-catalogo.sql", "/db/schema-seguridad.sql", "/db/schema-prestamos.sql");

        libroRepo = new JdbcLibroRepository(db);
        ejemplarRepo = new JdbcEjemplarRepository(db);
        mantenimientoRepo = new JdbcMantenimientoRepository(db);
        lectorRepo = new JdbcLectorRepository(db);
        prestamoRepo = new JdbcPrestamoRepository(db);
        usuarioRepo = new JdbcUsuarioSistemaRepository(db);

        Clock reloj = Clock.systemDefaultZone();
        catalogoService = new CatalogoService(libroRepo, ejemplarRepo);
        mantenimientoService = new MantenimientoService(db, ejemplarRepo, mantenimientoRepo);
        prestamoService = new PrestamoService(db, lectorRepo, ejemplarRepo, prestamoRepo, reloj);
        lectorService = new LectorService(db, lectorRepo, prestamoRepo);
        autenticacionService = new AutenticacionService(usuarioRepo);
        dashboardService = new DashboardService(libroRepo, ejemplarRepo, prestamoService);

        // Si no existe ninguna cuenta en la BD, creamos el administrador inicial por defecto
        if (!usuarioRepo.existeAlgunaCuenta()) {
            autenticacionService.crearAdministradorInicial("admin", "Admin123*".toCharArray());
        }
    }

    /** Abre la ventana de inicio de sesión. */
    private static void mostrarLogin() {
        LoginFrame login = new LoginFrame();
        new LoginController(login, autenticacionService, sesion -> {
            login.dispose();
            abrirVentanaPrincipal(sesion);
        });
        login.setLocationRelativeTo(null);
        login.setVisible(true);
    }

    /** Abre la ventana principal y conecta cada panel con su controlador. */
    private static void abrirVentanaPrincipal(Sesion sesion) {
        PublicadorEventos eventos = new PublicadorEventos();
        boolean esAdmin = sesion.getUsuario().tieneRol(Rol.ADMINISTRADOR);

        MainFrame ventana = new MainFrame(
                sesion.getUsuario().getUsuario(),
                sesion.getUsuario().getRol().name(),
                esAdmin
        );

        BibliotecaFacade facade = new BibliotecaFacade(db, prestamoService, mantenimientoService, evento -> {
            if (BibliotecaFacade.EVENTO_PRESTAMO_REGISTRADO.equals(evento)
                    || BibliotecaFacade.EVENTO_DEVOLUCION_REGISTRADA.equals(evento)) {
                eventos.publicar(TipoEvento.PRESTAMOS_CAMBIO);
            }
            if (BibliotecaFacade.EVENTO_MANTENIMIENTO_ACTUALIZADO.equals(evento)) {
                eventos.publicar(TipoEvento.MANTENIMIENTO_CAMBIO);
            }
        });

        GestionCuentas gestionCuentas = GestionCuentasFactory.crear(usuarioRepo, sesion);

        new DashboardController(ventana.getDashboardPanel(), dashboardService, eventos);
        new LibroController(ventana.getLibrosPanel(), catalogoService, libroRepo, eventos);
        new LectorController(ventana.getLectoresPanel(), lectorService, lectorRepo, eventos);
        new PrestamoController(ventana.getPrestamosPanel(), facade, eventos, 1L);
        new MantenimientoController(ventana.getMantenimientoPanel(), facade, ejemplarRepo, mantenimientoRepo, eventos);
        if (esAdmin) {
            new PersonalController(ventana.getPersonalPanel(), gestionCuentas, db, eventos);
        }

        ventana.addCerrarSesionListener(e -> {
            autenticacionService.cerrarSesion(sesion);
            ventana.dispose();
            mostrarLogin();
        });

        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    /** Usa el estilo Nimbus si está disponible. */
    private static void usarEstiloNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Si falla, se queda con el estilo por defecto del sistema
        }
    }
}
