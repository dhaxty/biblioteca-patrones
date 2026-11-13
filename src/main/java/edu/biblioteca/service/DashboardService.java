package edu.biblioteca.service;

import edu.biblioteca.model.estado.TipoEstado;
import edu.biblioteca.repository.EjemplarRepository;
import edu.biblioteca.repository.LibroRepository;

import java.util.Objects;

public class DashboardService {

    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final PrestamoService prestamoService;

    public DashboardService(LibroRepository libroRepository,
                            EjemplarRepository ejemplarRepository,
                            PrestamoService prestamoService) {
        this.libroRepository = Objects.requireNonNull(libroRepository, "LibroRepository no puede ser nulo");
        this.ejemplarRepository = Objects.requireNonNull(ejemplarRepository, "EjemplarRepository no puede ser nulo");
        this.prestamoService = Objects.requireNonNull(prestamoService, "PrestamoService no puede ser nulo");
    }

    public Indicadores obtenerIndicadores() {
        int totalLibros = (int) libroRepository.contarActivos();
        int ejemplaresDisponibles = (int) ejemplarRepository.contarPorEstado(TipoEstado.DISPONIBLE);
        int prestamosActivos = (int) prestamoService.contarActivos();
        int prestamosVencidos = (int) prestamoService.contarVencidos();
        return new Indicadores(totalLibros, ejemplaresDisponibles, prestamosActivos, prestamosVencidos);
    }
}
