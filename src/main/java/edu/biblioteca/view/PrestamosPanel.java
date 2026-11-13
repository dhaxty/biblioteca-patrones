package edu.biblioteca.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PrestamosPanel extends PanelBase {

    // Prestar
    private final JTextField txtCodigoLector = new JTextField(8);
    private final JTextField txtCodigoEjemplarPrestar = new JTextField(8);
    private final JButton btnPrestar = new JButton("Prestar");

    // Devolver
    private final JTextField txtCodigoEjemplarDevolver = new JTextField(8);
    private final JCheckBox chkDanado = new JCheckBox("Devuelto dañado");
    private final JButton btnDevolver = new JButton("Devolver");

    // Consultas
    private final JButton btnVerActivos = new JButton("Ver activos");
    private final JButton btnVerVencidos = new JButton("Ver vencidos");

    private final DefaultTableModel modelo = crearModelo("Lector", "Ejemplar", "Inicio", "Límite", "Operador");
    private final JTable tabla = crearTabla(modelo);
    private final JScrollPane scrollTabla = new JScrollPane(tabla);

    public PrestamosPanel() {
        JPanel formulario = apilar(
                fila(new JLabel("Código de lector"), txtCodigoLector,
                        new JLabel("Código de ejemplar"), txtCodigoEjemplarPrestar, btnPrestar),
                fila(new JLabel("Código de ejemplar a devolver"), txtCodigoEjemplarDevolver,
                        chkDanado, btnDevolver),
                fila(btnVerActivos, btnVerVencidos));
        add(formulario, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
    }

    // ----- Lo que la persona escribió o marcó -----

    public String getCodigoLector() {
        return txtCodigoLector.getText().trim();
    }

    public String getCodigoEjemplarPrestar() {
        return txtCodigoEjemplarPrestar.getText().trim();
    }

    public String getCodigoEjemplarDevolver() {
        return txtCodigoEjemplarDevolver.getText().trim();
    }

    public boolean isDanado() {
        return chkDanado.isSelected();
    }

    // ----- Lo que el controlador pide mostrar -----

    /**
     * Llena la tabla y le pone un título (por ejemplo "Préstamos activos").
     */
    public void mostrarPrestamos(List<String[]> filas, String titulo) {
        llenar(modelo, filas);
        scrollTabla.setBorder(BorderFactory.createTitledBorder(titulo));
    }

    public void limpiarPrestar() {
        txtCodigoLector.setText("");
        txtCodigoEjemplarPrestar.setText("");
    }

    public void limpiarDevolver() {
        txtCodigoEjemplarDevolver.setText("");
        chkDanado.setSelected(false);
    }

    // ----- Puertas para que el controlador escuche los clics -----

    public void addPrestarListener(ActionListener l) {
        btnPrestar.addActionListener(l);
    }

    public void addDevolverListener(ActionListener l) {
        btnDevolver.addActionListener(l);
    }

    public void addVerActivosListener(ActionListener l) {
        btnVerActivos.addActionListener(l);
    }

    public void addVerVencidosListener(ActionListener l) {
        btnVerVencidos.addActionListener(l);
    }
}
