package biblioteca.repository;

import biblioteca.model.RegistroMantenimiento;

import java.util.List;
import java.util.Optional;

public interface MantenimientoRepository {

    RegistroMantenimiento guardar(RegistroMantenimiento registro);

    /** Persiste la fecha de habilitación de un registro existente. */
    void actualizar(RegistroMantenimiento registro);

    Optional<RegistroMantenimiento> buscarPorId(long id);

    /** Registro sin fecha de habilitación para el ejemplar, si existe. */
    Optional<RegistroMantenimiento> buscarAbiertoPorEjemplar(long ejemplarId);

    /** Historial completo del ejemplar, del más antiguo al más reciente. */
    List<RegistroMantenimiento> listarPorEjemplar(long ejemplarId);
}
