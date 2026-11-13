package edu.biblioteca.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MantenimientoPanel extends PanelBase {

    private final JTextField txtCodigoEjemplar = new JTextField(8);
    private final JTextField txtMotivo = new JTextField(22);
    private final JButton btnRetirar = new JButton("Retirar a mantenimiento");
    private final JButton btnHabilitar = new JButton("Habilitar ejemplar");

    private final DefaultTableModel modelo = crearModelo("Ejemplar", "Motivo", "Ingreso", "Habilitación");
    private final JTable tabla = crearTabla(modelo);

    public MantenimientoPanel() {
        JPanel formulario = apilar(
                fila(new JLabel("Código del ejemplar"), txtCodigoEjemplar,
                        new JLabel("Motivo"), txtMotivo, btnRetirar, btnHabilitar));
        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    /**
     * Devuelve el código escrito; si está vacío, usa el ejemplar de la fila
     * seleccionada en el historial (comodidad para habilitar rápido).
     */
    public String getCodigoEjemplar() {
        String escrito = txtCodigoEjemplar.getText().trim();
        if (!escrito.isEmpty()) {
            return escrito;
        }
        String seleccionado = valorSeleccionado(tabla, 0);
        return seleccionado == null ? "" : seleccionado;
    }

    public String getMotivo() {
        return txtMotivo.getText().trim();
    }

    public void mostrarHistorial(List<String[]> filas) {
        llenar(modelo, filas);
    }

    public void limpiarFormulario() {
        txtCodigoEjemplar.setText("");
        txtMotivo.setText("");
    }

    public void addRetirarListener(ActionListener l) {
        btnRetirar.addActionListener(l);
    }

    public void addHabilitarListener(ActionListener l) {
        btnHabilitar.addActionListener(l);
    }
}
