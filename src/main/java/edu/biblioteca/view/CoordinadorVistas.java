package edu.biblioteca.view;

import edu.biblioteca.config.ContextoAplicacion;
import edu.biblioteca.controller.DashboardController;
import edu.biblioteca.controller.LectorController;
import edu.biblioteca.controller.LibroController;
import edu.biblioteca.controller.LoginController;
import edu.biblioteca.controller.MantenimientoController;
import edu.biblioteca.controller.PersonalController;
import edu.biblioteca.controller.PrestamoController;
import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.security.GestionCuentas;
import edu.biblioteca.security.GestionCuentasFactory;
import edu.biblioteca.security.Rol;
import edu.biblioteca.security.Sesion;
import edu.biblioteca.service.BibliotecaFacade;

import javax.swing.UIManager;
import java.util.Objects;

/**
 * Coordina el flujo de navegación de la interfaz gráfica Swing (LoginFrame ↔ MainFrame),
 * gestiona el ciclo de vida de la sesión y realiza el enlace con los controladores MVC.
 */
public class CoordinadorVistas {

    private final ContextoAplicacion contexto;

    public CoordinadorVistas(ContextoAplicacion contexto) {
        this.contexto = Objects.requireNonNull(contexto, "ContextoAplicacion no puede ser nulo");
    }

    /**
     * Aplica el Look & Feel Nimbus si está disponible en la plataforma.
     */
    public static void configurarApariencia() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Se mantiene el estilo por defecto del sistema
        }
    }

    /**
     * Inicia la interfaz gráfica mostrando la pantalla de login.
     */
    public void iniciar() {
        mostrarLogin();
    }

    private void mostrarLogin() {
        LoginFrame login = new LoginFrame();
        new LoginController(login, contexto.getAutenticacionService(), sesion -> {
            login.dispose();
            abrirVentanaPrincipal(sesion);
        });
        login.setLocationRelativeTo(null);
        login.setVisible(true);
    }

    private void abrirVentanaPrincipal(Sesion sesion) {
        PublicadorEventos eventos = new PublicadorEventos();
        boolean esAdmin = sesion.getUsuario().tieneRol(Rol.ADMINISTRADOR);

        MainFrame ventana = new MainFrame(
                sesion.getUsuario().getUsuario(),
                sesion.getUsuario().getRol().name(),
                esAdmin
        );

        BibliotecaFacade facade = new BibliotecaFacade(
                contexto.getDatabase(),
                contexto.getCatalogoService(),
                contexto.getPrestamoService(),
                contexto.getMantenimientoService(),
                evento -> {
                    if (BibliotecaFacade.EVENTO_PRESTAMO_REGISTRADO.equals(evento)
                            || BibliotecaFacade.EVENTO_DEVOLUCION_REGISTRADA.equals(evento)) {
                        eventos.publicar(TipoEvento.PRESTAMOS_CAMBIO);
                    }
                    if (BibliotecaFacade.EVENTO_MANTENIMIENTO_ACTUALIZADO.equals(evento)) {
                        eventos.publicar(TipoEvento.MANTENIMIENTO_CAMBIO);
                    }
                }
        );

        GestionCuentas gestionCuentas = GestionCuentasFactory.crear(contexto.getUsuarioRepo(), sesion);

        new DashboardController(ventana.getDashboardPanel(), contexto.getDashboardService(), eventos);
        new LibroController(ventana.getLibrosPanel(), contexto.getCatalogoService(), eventos);
        new LectorController(ventana.getLectoresPanel(), contexto.getLectorService(), eventos);
        new PrestamoController(ventana.getPrestamosPanel(), facade, eventos, 1L);
        new MantenimientoController(ventana.getMantenimientoPanel(), facade, contexto.getMantenimientoService(), eventos);
        if (esAdmin) {
            new PersonalController(ventana.getPersonalPanel(), gestionCuentas, eventos);
        }

        ventana.addCerrarSesionListener(e -> {
            contexto.getAutenticacionService().cerrarSesion(sesion);
            ventana.dispose();
            mostrarLogin();
        });

        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }
}
