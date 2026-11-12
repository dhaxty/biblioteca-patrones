package edu.biblioteca.repository;

import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.estado.TipoEstado;

import java.util.List;
import java.util.Optional;

public interface EjemplarRepository {

    /** Inserta el ejemplar y le asigna su id. */
    Ejemplar guardar(Ejemplar ejemplar);

    Optional<Ejemplar> buscarPorId(long id);

    Optional<Ejemplar> buscarPorCodigo(String codigo);

    List<Ejemplar> listar();

    List<Ejemplar> listarPorLibro(long libroId);

    List<Ejemplar> listarPorEstado(TipoEstado estado);

    /** Persiste el estado actual del ejemplar. */
    void actualizar(Ejemplar ejemplar);

    /** Para el Dashboard: por ejemplo, ejemplares disponibles. */
    long contarPorEstado(TipoEstado estado);
}
