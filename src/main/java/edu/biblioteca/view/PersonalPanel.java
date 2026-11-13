package edu.biblioteca.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PersonalPanel extends PanelBase {

    private final JTextField txtUsuario = new JTextField(10);
    private final JPasswordField txtClave = new JPasswordField(10);
    private final JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Bibliotecario", "Administrador"});
    private final JButton btnCrear = new JButton("Crear cuenta");
    private final JButton btnDesactivar = new JButton("Desactivar seleccionada");

    private final DefaultTableModel modelo = crearModelo("Usuario", "Rol", "Activo");
    private final JTable tabla = crearTabla(modelo);

    public PersonalPanel() {
        JPanel formulario = apilar(
                fila(new JLabel("Usuario"), txtUsuario, new JLabel("Contraseña"), txtClave,
                        new JLabel("Rol"), cmbRol, btnCrear, btnDesactivar));
        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public String getUsuario() {
        return txtUsuario.getText().trim();
    }

    public String getClave() {
        return new String(txtClave.getPassword());
    }

    public String getRol() {
        return String.valueOf(cmbRol.getSelectedItem());
    }

    /** @return usuario de la cuenta seleccionada, o null si no hay selección */
    public String getUsuarioSeleccionado() {
        return valorSeleccionado(tabla, 0);
    }

    public void mostrarCuentas(List<String[]> filas) {
        llenar(modelo, filas);
    }

    public void limpiarFormulario() {
        txtUsuario.setText("");
        txtClave.setText("");
        cmbRol.setSelectedIndex(0);
    }

    public void addCrearListener(ActionListener l) {
        btnCrear.addActionListener(l);
    }

    public void addDesactivarListener(ActionListener l) {
        btnDesactivar.addActionListener(l);
    }
}
