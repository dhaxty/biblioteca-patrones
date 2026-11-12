package edu.biblioteca.repository;

import edu.biblioteca.model.RegistroMantenimiento;

import java.util.List;
import java.util.Optional;

public interface MantenimientoRepository {

    /** Inserta un registro nuevo (ingreso a mantenimiento) y le asigna su id. */
    RegistroMantenimiento abrir(RegistroMantenimiento registro);

    /** Guarda la fecha de habilitación de un registro ya existente. */
    void finalizar(RegistroMantenimiento registro);

    Optional<RegistroMantenimiento> buscarAbiertoPorEjemplar(long ejemplarId);

    /** Historial completo del ejemplar, del más reciente al más antiguo. */
    List<RegistroMantenimiento> listarPorEjemplar(long ejemplarId);
}
