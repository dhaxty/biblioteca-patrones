package edu.biblioteca.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

    private final DashboardPanel dashboardPanel = new DashboardPanel();
    private final LibrosPanel librosPanel = new LibrosPanel();
    private final LectoresPanel lectoresPanel = new LectoresPanel();
    private final PrestamosPanel prestamosPanel = new PrestamosPanel();
    private final MantenimientoPanel mantenimientoPanel = new MantenimientoPanel();
    private final PersonalPanel personalPanel = new PersonalPanel();
    private final JButton btnCerrarSesion = new JButton("Cerrar sesión");

    /**
     * @param usuario         nombre de quien inició sesión
     * @param rol             rol de esa persona
     * @param esAdministrador si es true, se muestra la pestaña Personal
     */
    public MainFrame(String usuario, String rol, boolean esAdministrador) {
        super("Biblioteca Universitaria");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Barra superior con la sesión actual y el botón de cerrar sesión
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        barra.add(new JLabel("Sesión: " + usuario + " (" + rol + ")"), BorderLayout.WEST);
        barra.add(btnCerrarSesion, BorderLayout.EAST);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Dashboard", dashboardPanel);
        pestanas.addTab("Libros", librosPanel);
        pestanas.addTab("Lectores", lectoresPanel);
        pestanas.addTab("Préstamos", prestamosPanel);
        pestanas.addTab("Mantenimiento", mantenimientoPanel);
        if (esAdministrador) {
            pestanas.addTab("Personal", personalPanel);
        }

        add(barra, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        setSize(1100, 650);
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public LibrosPanel getLibrosPanel() {
        return librosPanel;
    }

    public LectoresPanel getLectoresPanel() {
        return lectoresPanel;
    }

    public PrestamosPanel getPrestamosPanel() {
        return prestamosPanel;
    }

    public MantenimientoPanel getMantenimientoPanel() {
        return mantenimientoPanel;
    }

    public PersonalPanel getPersonalPanel() {
        return personalPanel;
    }

    /** Puerta para que Main escuche el clic de "Cerrar sesión". */
    public void addCerrarSesionListener(ActionListener l) {
        btnCerrarSesion.addActionListener(l);
    }
}
