package edu.biblioteca.controller;

import edu.biblioteca.event.ObservadorDashboard;
import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.service.DashboardService;
import edu.biblioteca.service.Indicadores;
import edu.biblioteca.view.DashboardPanel;

public class DashboardController {

    private final DashboardPanel vista;
    private final DashboardService servicio;

    public DashboardController(DashboardPanel vista, DashboardService servicio, PublicadorEventos eventos) {
        this.vista = vista;
        this.servicio = servicio;

        // Observer: registrar al observador del dashboard en el publicador
        eventos.suscribir(new ObservadorDashboard(this::actualizar));

        actualizar();
    }

    /** Consulta los indicadores y los muestra. */
    private void actualizar() {
        Indicadores i = servicio.obtenerIndicadores();
        vista.mostrarIndicadores(i.totalLibros(), i.ejemplaresDisponibles(),
                i.prestamosActivos(), i.prestamosVencidos());
    }
}
