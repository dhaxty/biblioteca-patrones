package edu.biblioteca.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class LectoresPanel extends PanelBase {

    private final JTextField txtCodigo = new JTextField(8);
    private final JTextField txtNombre = new JTextField(14);
    private final JTextField txtCorreo = new JTextField(16);
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Estudiante", "Docente"});
    private final JButton btnRegistrar = new JButton("Registrar lector");

    private final JTextField txtBuscar = new JTextField(16);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnDesactivar = new JButton("Desactivar seleccionado");

    private final DefaultTableModel modelo = crearModelo("Código", "Nombre", "Correo", "Tipo");
    private final JTable tabla = crearTabla(modelo);

    public LectoresPanel() {
        JPanel formulario = apilar(
                fila(new JLabel("Código"), txtCodigo, new JLabel("Nombre"), txtNombre,
                        new JLabel("Correo"), txtCorreo, new JLabel("Tipo"), cmbTipo, btnRegistrar),
                fila(new JLabel("Buscar (código o nombre)"), txtBuscar, btnBuscar, btnDesactivar));
        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    // ----- Lo que la persona escribió o seleccionó -----

    public String getCodigo() {
        return txtCodigo.getText().trim();
    }

    public String getNombre() {
        return txtNombre.getText().trim();
    }

    public String getCorreo() {
        return txtCorreo.getText().trim();
    }

    public String getTipo() {
        return String.valueOf(cmbTipo.getSelectedItem());
    }

    public String getTextoBusqueda() {
        return txtBuscar.getText().trim();
    }

    /** @return código del lector seleccionado, o null si no hay selección */
    public String getCodigoSeleccionado() {
        return valorSeleccionado(tabla, 0);
    }

    // ----- Lo que el controlador pide mostrar -----

    public void mostrarLectores(List<String[]> filas) {
        llenar(modelo, filas);
    }

    public void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");
        cmbTipo.setSelectedIndex(0);
    }

    // ----- Puertas para que el controlador escuche los clics -----

    public void addRegistrarListener(ActionListener l) {
        btnRegistrar.addActionListener(l);
    }

    public void addBuscarListener(ActionListener l) {
        btnBuscar.addActionListener(l);
    }

    public void addDesactivarListener(ActionListener l) {
        btnDesactivar.addActionListener(l);
    }
}
