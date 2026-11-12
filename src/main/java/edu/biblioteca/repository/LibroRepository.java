package edu.biblioteca.repository;

import edu.biblioteca.model.Libro;

import java.util.List;
import java.util.Optional;

public interface LibroRepository {

    /** Inserta el libro y le asigna su id. */
    Libro guardar(Libro libro);

    Optional<Libro> buscarPorId(long id);

    Optional<Libro> buscarPorIsbn(String isbn);

    List<Libro> listar();

    /** Busca por título, autor, ISBN o código de ejemplar (coincidencia parcial, sin distinguir mayúsculas). */
    List<Libro> buscar(String texto, boolean soloActivos);

    void actualizar(Libro libro);

    /** Para el Dashboard: total de libros activos. */
    long contarActivos();
}
