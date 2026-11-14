package edu.biblioteca.controller;

import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.model.RegistroMantenimientoDetalle;
import edu.biblioteca.service.BibliotecaOperaciones;
import edu.biblioteca.service.MantenimientoService;
import edu.biblioteca.service.ReglaNegocioException;
import edu.biblioteca.view.MantenimientoPanel;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MantenimientoController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MantenimientoPanel vista;
    private final BibliotecaOperaciones facade;
    private final MantenimientoService servicio;
    private final PublicadorEventos eventos;

    public MantenimientoController(MantenimientoPanel vista, BibliotecaOperaciones facade,
                                   MantenimientoService servicio, PublicadorEventos eventos) {
        this.vista = vista;
        this.facade = facade;
        this.servicio = servicio;
        this.eventos = eventos;

        vista.addRetirarListener(e -> retirar());
        vista.addHabilitarListener(e -> habilitar());

        // Observer: se refresca el historial cuando cambia el mantenimiento
        eventos.suscribir(tipo -> {
            if (tipo == TipoEvento.MANTENIMIENTO_CAMBIO) {
                refrescar();
            }
        });

        refrescar();
    }

    private void retirar() {
        try {
            facade.retirarEjemplarAMantenimiento(vista.getCodigoEjemplar(), vista.getMotivo());
            vista.limpiarFormulario();
            vista.mostrarMensaje("Ejemplar retirado a mantenimiento.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al retirar ejemplar: " + ex.getMessage());
        }
    }

    private void habilitar() {
        try {
            facade.habilitarEjemplar(vista.getCodigoEjemplar());
            vista.limpiarFormulario();
            vista.mostrarMensaje("Ejemplar habilitado nuevamente.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al habilitar ejemplar: " + ex.getMessage());
        }
    }

    private void refrescar() {
        if (servicio == null) {
            return;
        }
        try {
            List<String[]> filas = new ArrayList<>();
            for (RegistroMantenimientoDetalle det : servicio.consultarHistorialCompleto()) {
                filas.add(new String[]{
                    det.codigoEjemplar(),
                    det.motivo(),
                    det.ingreso() != null ? det.ingreso().format(FORMATO_FECHA) : "",
                    det.habilitacion() != null ? det.habilitacion().format(FORMATO_FECHA) : "En curso"
                });
            }
            vista.mostrarHistorial(filas);
        } catch (Exception ex) {
            vista.mostrarError("Error al consultar historial: " + ex.getMessage());
        }
    }
}