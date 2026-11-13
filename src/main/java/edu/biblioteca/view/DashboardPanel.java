package edu.biblioteca.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardPanel extends JPanel {

    private final JLabel lblLibros = crearNumero();
    private final JLabel lblDisponibles = crearNumero();
    private final JLabel lblActivos = crearNumero();
    private final JLabel lblVencidos = crearNumero();

    public DashboardPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        // Las cuatro tarjetas en una fila, pegadas arriba y con altura moderada
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 16, 16));
        tarjetas.add(crearTarjeta("Libros registrados", lblLibros));
        tarjetas.add(crearTarjeta("Ejemplares disponibles", lblDisponibles));
        tarjetas.add(crearTarjeta("Préstamos activos", lblActivos));
        tarjetas.add(crearTarjeta("Préstamos vencidos", lblVencidos));
        add(tarjetas, BorderLayout.NORTH);
    }

    /** Actualiza los cuatro números que se ven en pantalla. */
    public void mostrarIndicadores(int libros, int disponibles, int activos, int vencidos) {
        lblLibros.setText(String.valueOf(libros));
        lblDisponibles.setText(String.valueOf(disponibles));
        lblActivos.setText(String.valueOf(activos));
        lblVencidos.setText(String.valueOf(vencidos));
    }

    private static JLabel crearNumero() {
        JLabel etiqueta = new JLabel("0", SwingConstants.CENTER);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 40f));
        return etiqueta;
    }

    private static JPanel crearTarjeta(String titulo, JLabel numero) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createTitledBorder(titulo));
        tarjeta.setPreferredSize(new Dimension(200, 150));
        tarjeta.add(numero, BorderLayout.CENTER);
        return tarjeta;
    }
}
