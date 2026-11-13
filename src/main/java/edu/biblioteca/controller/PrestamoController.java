package edu.biblioteca.controller;

import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.model.PrestamoDetalle;
import edu.biblioteca.service.BibliotecaOperaciones;
import edu.biblioteca.service.ReglaNegocioException;
import edu.biblioteca.view.PrestamosPanel;

import java.util.List;

public class PrestamoController {

    private final PrestamosPanel vista;
    private final BibliotecaOperaciones facade;
    private final PublicadorEventos eventos;
    private final long operadorId;

    /** Recuerda qué lista se está viendo para refrescar la correcta. */
    private boolean mostrandoVencidos = false;

    public PrestamoController(PrestamosPanel vista, BibliotecaOperaciones facade,
                              PublicadorEventos eventos, long operadorId) {
        this.vista = vista;
        this.facade = facade;
        this.eventos = eventos;
        this.operadorId = operadorId;

        vista.addPrestarListener(e -> prestar());
        vista.addDevolverListener(e -> devolver());
        vista.addVerActivosListener(e -> {
            mostrandoVencidos = false;
            refrescar();
        });
        vista.addVerVencidosListener(e -> {
            mostrandoVencidos = true;
            refrescar();
        });

        // Observer: se refresca cuando hay un préstamo o una devolución confirmados
        eventos.suscribir(tipo -> {
            if (tipo == TipoEvento.PRESTAMOS_CAMBIO) {
                refrescar();
            }
        });

        refrescar();
    }

    public PrestamoController(PrestamosPanel vista, BibliotecaOperaciones facade, PublicadorEventos eventos) {
        this(vista, facade, eventos, 1L);
    }

    private void prestar() {
        try {
            facade.prestar(vista.getCodigoLector(), vista.getCodigoEjemplarPrestar(), operadorId);
            vista.limpiarPrestar();
            eventos.publicar(TipoEvento.PRESTAMOS_CAMBIO); // solo después de guardar con éxito
            vista.mostrarMensaje("Préstamo registrado correctamente.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al registrar préstamo: " + ex.getMessage());
        }
    }

    private void devolver() {
        try {
            boolean danado = vista.isDanado();
            String motivo = danado ? "Devuelto con daño por el usuario" : null;
            facade.devolver(vista.getCodigoEjemplarDevolver(), danado, motivo);
            vista.limpiarDevolver();
            // La devolución puede mandar el ejemplar a mantenimiento: avisamos a ambos.
            eventos.publicar(TipoEvento.PRESTAMOS_CAMBIO);
            if (danado) {
                eventos.publicar(TipoEvento.MANTENIMIENTO_CAMBIO);
            }
            vista.mostrarMensaje("Devolución registrada correctamente.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al registrar devolución: " + ex.getMessage());
        }
    }

    /** Vuelve a cargar la tabla con la lista que se estaba viendo. */
    private void refrescar() {
        List<PrestamoDetalle> detalles = mostrandoVencidos ? facade.listarVencidos() : facade.listarActivos();
        String titulo = mostrandoVencidos ? "Préstamos vencidos" : "Préstamos activos";

        List<String[]> filas = detalles.stream()
                .map(d -> new String[]{
                    d.codigoLector(),
                    d.codigoEjemplar(),
                    String.valueOf(d.fechaInicio()),
                    String.valueOf(d.fechaLimite()),
                    d.vencido() ? "Vencido" : "Al día"
                })
                .toList();

        vista.mostrarPrestamos(filas, titulo);
    }
}
