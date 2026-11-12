package biblioteca.repository;

import biblioteca.model.Ejemplar;

import java.util.List;
import java.util.Optional;

/** Contrato usado por el Módulo B (Préstamos). No cambiar firmas públicas sin previo aviso. */
public interface EjemplarRepository {

    /** Inserta el ejemplar y le asigna el id generado. */
    Ejemplar guardar(Ejemplar ejemplar);

    /** Persiste el estado (y datos) de un ejemplar existente. */
    void actualizar(Ejemplar ejemplar);

    Optional<Ejemplar> buscarPorId(long id);

    Optional<Ejemplar> buscarPorCodigo(String codigoUnico);

    List<Ejemplar> buscarPorLibro(long libroId);

    List<Ejemplar> listarTodos();
}
