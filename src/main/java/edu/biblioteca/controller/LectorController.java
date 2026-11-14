package edu.biblioteca.controller;

import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.model.Lector;
import edu.biblioteca.model.TipoLector;
import edu.biblioteca.service.LectorService;
import edu.biblioteca.service.ReglaNegocioException;
import edu.biblioteca.view.LectoresPanel;

import java.util.List;

public class LectorController {

    private final LectoresPanel vista;
    private final LectorService servicio;
    private final PublicadorEventos eventos;

    public LectorController(LectoresPanel vista, LectorService servicio, PublicadorEventos eventos) {
        this.vista = vista;
        this.servicio = servicio;
        this.eventos = eventos;

        vista.addRegistrarListener(e -> registrar());
        vista.addBuscarListener(e -> refrescar());
        vista.addDesactivarListener(e -> desactivar());

        // Observer: la tabla se actualiza cuando cambian los lectores
        eventos.suscribir(tipo -> {
            if (tipo == TipoEvento.LECTORES_CAMBIO) {
                refrescar();
            }
        });

        refrescar();
    }

    private void registrar() {
        try {
            TipoLector tipo = "Docente".equalsIgnoreCase(vista.getTipo()) ? TipoLector.DOCENTE : TipoLector.ESTUDIANTE;
            servicio.registrarLector(vista.getCodigo(), vista.getNombre(), vista.getCorreo(), tipo);
            vista.limpiarFormulario();
            eventos.publicar(TipoEvento.LECTORES_CAMBIO);
            vista.mostrarMensaje("Lector registrado correctamente.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al registrar lector: " + ex.getMessage());
        }
    }

    private void desactivar() {
        String codigo = vista.getCodigoSeleccionado();
        if (codigo == null) {
            vista.mostrarError("Seleccione primero un lector de la tabla.");
            return;
        }
        try {
            servicio.desactivarLector(codigo);
            eventos.publicar(TipoEvento.LECTORES_CAMBIO);
            vista.mostrarMensaje("Lector desactivado (su historial se conserva).");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al desactivar lector: " + ex.getMessage());
        }
    }

    private void refrescar() {
        List<Lector> lista = servicio.buscarLector(vista.getTextoBusqueda());
        List<String[]> filas = lista.stream()
                .map(l -> new String[]{
                    l.getCodigoUniversitario(),
                    l.getNombre(),
                    l.getCorreo(),
                    l.getTipo() == TipoLector.DOCENTE ? "Docente" : "Estudiante"
                })
                .toList();
        vista.mostrarLectores(filas);
    }
}
