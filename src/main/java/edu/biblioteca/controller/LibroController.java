package edu.biblioteca.controller;

import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.Libro;
import edu.biblioteca.service.CatalogoService;
import edu.biblioteca.service.ReglaNegocioException;
import edu.biblioteca.view.LibrosPanel;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LibroController {

    private final LibrosPanel vista;
    private final CatalogoService servicio;
    private final PublicadorEventos eventos;

    public LibroController(LibrosPanel vista, CatalogoService servicio, PublicadorEventos eventos) {
        this.vista = vista;
        this.servicio = servicio;
        this.eventos = eventos;

        // Conectar los botones de la vista con las acciones de este controlador
        vista.addRegistrarListener(e -> registrar());
        vista.addBuscarListener(e -> refrescar());
        vista.addDesactivarListener(e -> desactivar());
        vista.addAgregarEjemplarListener(e -> agregarEjemplar());
        vista.addSeleccionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarEjemplares();
            }
        });

        // Observer: si cambia el catálogo, los préstamos o el mantenimiento, se refresca la tabla
        eventos.suscribir(tipo -> {
            if (tipo == TipoEvento.CATALOGO_CAMBIO || tipo == TipoEvento.PRESTAMOS_CAMBIO
                    || tipo == TipoEvento.MANTENIMIENTO_CAMBIO) {
                refrescar();
            }
        });

        refrescar();
    }

    private void registrar() {
        try {
            int anioActual = Year.now().getValue();
            servicio.registrarLibro(vista.getIsbn(), vista.getTitulo(), vista.getAutor(), "Editorial", anioActual, vista.getCategoria());
            vista.limpiarFormulario();
            eventos.publicar(TipoEvento.CATALOGO_CAMBIO); // solo después de guardar con éxito
            vista.mostrarMensaje("Libro registrado correctamente.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al registrar: " + ex.getMessage());
        }
    }

    private void agregarEjemplar() {
        String isbn = vista.getIsbnSeleccionado();
        if (isbn == null) {
            vista.mostrarError("Seleccione primero un libro de la tabla.");
            return;
        }
        try {
            servicio.agregarEjemplar(isbn, vista.getCodigoEjemplar());
            vista.limpiarFormulario();
            eventos.publicar(TipoEvento.CATALOGO_CAMBIO);
            vista.mostrarMensaje("Ejemplar agregado correctamente.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al agregar ejemplar: " + ex.getMessage());
        }
    }

    private void desactivar() {
        String isbn = vista.getIsbnSeleccionado();
        if (isbn == null) {
            vista.mostrarError("Seleccione primero un libro de la tabla.");
            return;
        }
        try {
            servicio.desactivarLibro(isbn);
            eventos.publicar(TipoEvento.CATALOGO_CAMBIO);
            vista.mostrarMensaje("Libro desactivado (su historial se conserva).");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al desactivar libro: " + ex.getMessage());
        }
    }

    /** Vuelve a cargar la tabla de libros y conserva el libro seleccionado. */
    private void refrescar() {
        String seleccionado = vista.getIsbnSeleccionado();
        List<Libro> lista = servicio.buscarLibros(vista.getTextoBusqueda());
        List<String[]> filas = new ArrayList<>();
        for (Libro l : lista) {
            int cantidad = l.getId() != null ? servicio.listarEjemplares(l.getId()).size() : 0;
            filas.add(new String[]{
                l.getIsbn(),
                l.getTitulo(),
                l.getAutor(),
                l.getCategoria(),
                String.valueOf(cantidad)
            });
        }
        vista.mostrarLibros(filas);
        vista.seleccionarIsbn(seleccionado);
        cargarEjemplares();
    }

    /** Muestra los ejemplares del libro seleccionado (o ninguno si no hay selección). */
    private void cargarEjemplares() {
        String isbn = vista.getIsbnSeleccionado();
        if (isbn == null) {
            vista.mostrarEjemplares(Collections.emptyList());
        } else {
            List<Ejemplar> ejemplares = servicio.listarEjemplares(isbn);
            List<String[]> filas = ejemplares.stream()
                    .map(e -> new String[]{e.getCodigo(), e.getTipoEstado().etiqueta()})
                    .toList();
            vista.mostrarEjemplares(filas);
        }
    }
}
