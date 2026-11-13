package edu.biblioteca.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public abstract class PanelBase extends JPanel {

    /** Línea de mensajes (éxito o error) que se ve en la parte de abajo. */
    private final JLabel lblMensaje = new JLabel(" ");

    protected PanelBase() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblMensaje, BorderLayout.SOUTH);
    }

    /** Muestra un mensaje de éxito en verde. */
    public void mostrarMensaje(String texto) {
        lblMensaje.setForeground(new Color(0, 110, 40));
        lblMensaje.setText(vacioAEspacio(texto));
    }

    /** Muestra un mensaje de error en rojo. */
    public void mostrarError(String texto) {
        lblMensaje.setForeground(new Color(180, 0, 0));
        lblMensaje.setText(vacioAEspacio(texto));
    }

    private static String vacioAEspacio(String texto) {
        return (texto == null || texto.isBlank()) ? " " : texto;
    }

    // ----- Utilidades para las clases hijas -----

    /** Crea un modelo de tabla cuyas celdas no se pueden editar. */
    protected static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /** Crea una tabla que permite seleccionar una sola fila. */
    protected static JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        return tabla;
    }

    /** Reemplaza el contenido de la tabla por las filas recibidas. */
    protected static void llenar(DefaultTableModel modelo, List<String[]> filas) {
        modelo.setRowCount(0);
        for (String[] fila : filas) {
            modelo.addRow(fila);
        }
    }

    /** Una fila horizontal de componentes (etiquetas, campos, botones). */
    protected static JPanel fila(Component... componentes) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        for (Component componente : componentes) {
            panel.add(componente);
        }
        return panel;
    }

    /** Apila varias filas una debajo de otra. */
    protected static JPanel apilar(JPanel... filas) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (JPanel fila : filas) {
            panel.add(fila);
        }
        return panel;
    }

    /** Devuelve el valor de una columna de la fila seleccionada, o null si no hay selección. */
    protected static String valorSeleccionado(JTable tabla, int columna) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return String.valueOf(tabla.getValueAt(fila, columna));
    }
}
