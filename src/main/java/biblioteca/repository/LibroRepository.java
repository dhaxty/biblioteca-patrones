package biblioteca.repository;

import biblioteca.model.Libro;

import java.util.List;
import java.util.Optional;

public interface LibroRepository {

    /** Inserta el libro y le asigna el id generado. */
    Libro guardar(Libro libro);

    /** Actualiza los datos editables y el flag activo de un libro existente. */
    void actualizar(Libro libro);

    /** Desactivación lógica: UPDATE activo = 0. Nunca se usa DELETE. */
    void desactivar(long id);

    Optional<Libro> buscarPorId(long id);

    Optional<Libro> buscarPorIsbn(String isbn);

    List<Libro> listarTodos();

    List<Libro> listarActivos();
}
