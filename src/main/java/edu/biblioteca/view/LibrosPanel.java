package edu.biblioteca.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class LibrosPanel extends PanelBase {

    // Formulario de registro
    private final JTextField txtIsbn = new JTextField(10);
    private final JTextField txtTitulo = new JTextField(14);
    private final JTextField txtAutor = new JTextField(10);
    private final JTextField txtCategoria = new JTextField(8);
    private final JButton btnRegistrar = new JButton("Registrar libro");

    // Búsqueda y desactivación
    private final JTextField txtBuscar = new JTextField(16);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnDesactivar = new JButton("Desactivar seleccionado");

    // Ejemplares
    private final JTextField txtCodigoEjemplar = new JTextField(8);
    private final JButton btnAgregarEjemplar = new JButton("Agregar ejemplar al libro seleccionado");

    // Tablas
    private final DefaultTableModel modeloLibros = crearModelo("ISBN", "Título", "Autor", "Categoría", "Ejemplares");
    private final JTable tablaLibros = crearTabla(modeloLibros);
    private final DefaultTableModel modeloEjemplares = crearModelo("Código", "Estado");
    private final JTable tablaEjemplares = crearTabla(modeloEjemplares);

    public LibrosPanel() {
        JPanel formulario = apilar(
                fila(new JLabel("ISBN"), txtIsbn, new JLabel("Título"), txtTitulo,
                        new JLabel("Autor"), txtAutor, new JLabel("Categoría"), txtCategoria, btnRegistrar),
                fila(new JLabel("Buscar (título, autor, ISBN o código)"), txtBuscar, btnBuscar, btnDesactivar),
                fila(new JLabel("Código del ejemplar"), txtCodigoEjemplar, btnAgregarEjemplar));
        add(formulario, BorderLayout.NORTH);

        JScrollPane scrollLibros = new JScrollPane(tablaLibros);
        JScrollPane scrollEjemplares = new JScrollPane(tablaEjemplares);
        scrollEjemplares.setBorder(javax.swing.BorderFactory.createTitledBorder("Ejemplares del libro seleccionado"));
        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollLibros, scrollEjemplares);
        centro.setResizeWeight(0.7);
        add(centro, BorderLayout.CENTER);
    }

    // ----- Lo que la persona escribió o seleccionó -----

    public String getIsbn() {
        return txtIsbn.getText().trim();
    }

    public String getTitulo() {
        return txtTitulo.getText().trim();
    }

    public String getAutor() {
        return txtAutor.getText().trim();
    }

    public String getCategoria() {
        return txtCategoria.getText().trim();
    }

    public String getTextoBusqueda() {
        return txtBuscar.getText().trim();
    }

    public String getCodigoEjemplar() {
        return txtCodigoEjemplar.getText().trim();
    }

    /** @return ISBN del libro seleccionado en la tabla, o null si no hay selección */
    public String getIsbnSeleccionado() {
        return valorSeleccionado(tablaLibros, 0);
    }

    // ----- Lo que el controlador pide mostrar -----

    public void mostrarLibros(List<String[]> filas) {
        llenar(modeloLibros, filas);
    }

    public void mostrarEjemplares(List<String[]> filas) {
        llenar(modeloEjemplares, filas);
    }

    /** Vuelve a seleccionar un libro por su ISBN (útil después de refrescar la tabla). */
    public void seleccionarIsbn(String isbn) {
        if (isbn == null) {
            return;
        }
        for (int i = 0; i < modeloLibros.getRowCount(); i++) {
            if (isbn.equals(String.valueOf(modeloLibros.getValueAt(i, 0)))) {
                tablaLibros.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    /** Limpia los campos del formulario de registro. */
    public void limpiarFormulario() {
        txtIsbn.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtCategoria.setText("");
        txtCodigoEjemplar.setText("");
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

    public void addAgregarEjemplarListener(ActionListener l) {
        btnAgregarEjemplar.addActionListener(l);
    }

    public void addSeleccionListener(ListSelectionListener l) {
        tablaLibros.getSelectionModel().addListSelectionListener(l);
    }
}
