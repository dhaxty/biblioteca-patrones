package edu.biblioteca.controller;

import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.RegistroMantenimiento;
import edu.biblioteca.repository.EjemplarRepository;
import edu.biblioteca.repository.MantenimientoRepository;
import edu.biblioteca.service.BibliotecaOperaciones;
import edu.biblioteca.service.ReglaNegocioException;
import edu.biblioteca.view.MantenimientoPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MantenimientoController {

    private final MantenimientoPanel vista;
    private final BibliotecaOperaciones facade;
    private final EjemplarRepository ejemplares;
    private final MantenimientoRepository mantenimientos;
    private final PublicadorEventos eventos;

    public MantenimientoController(MantenimientoPanel vista, BibliotecaOperaciones facade,
                                   EjemplarRepository ejemplares, MantenimientoRepository mantenimientos,
                                   PublicadorEventos eventos) {
        this.vista = vista;
        this.facade = facade;
        this.ejemplares = ejemplares;
        this.mantenimientos = mantenimientos;
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

    public MantenimientoController(MantenimientoPanel vista, BibliotecaOperaciones facade, PublicadorEventos eventos) {
        this(vista, facade, null, null, eventos);
    }

    private void retirar() {
        try {
            facade.retirarEjemplarAMantenimiento(vista.getCodigoEjemplar(), vista.getMotivo());
            vista.limpiarFormulario();
            eventos.publicar(TipoEvento.MANTENIMIENTO_CAMBIO);
            eventos.publicar(TipoEvento.CATALOGO_CAMBIO);
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
            eventos.publicar(TipoEvento.MANTENIMIENTO_CAMBIO);
            eventos.publicar(TipoEvento.CATALOGO_CAMBIO);
            vista.mostrarMensaje("Ejemplar habilitado nuevamente.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al habilitar ejemplar: " + ex.getMessage());
        }
    }

    private void refrescar() {
        if (ejemplares == null || mantenimientos == null) {
            vista.mostrarHistorial(Collections.emptyList());
            return;
        }
        try {
            List<String[]> filas = new ArrayList<>();
            List<Ejemplar> listaEjemplares = ejemplares.listar();
            for (Ejemplar ej : listaEjemplares) {
                if (ej.getId() != null) {
                    List<RegistroMantenimiento> regs = mantenimientos.listarPorEjemplar(ej.getId());
                    for (RegistroMantenimiento rm : regs) {
                        filas.add(new String[]{
                            ej.getCodigo(),
                            rm.getMotivo(),
                            rm.getIngreso() != null ? rm.getIngreso().toString() : "",
                            rm.getHabilitacion() != null ? rm.getHabilitacion().toString() : "En curso"
                        });
                    }
                }
            }
            vista.mostrarHistorial(filas);
        } catch (Exception ex) {
            vista.mostrarError("Error al consultar historial: " + ex.getMessage());
        }
    }
}
