package biblioteca.service;

import biblioteca.model.Ejemplar;
import biblioteca.model.Libro;
import biblioteca.model.estado.EstadoPrestado;
import biblioteca.repository.EjemplarRepository;
import biblioteca.repository.LibroRepository;

import java.util.List;
import java.util.Optional;

/** CRUD lógico de libros y ejemplares. */
public class CatalogoService {

    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;

    public CatalogoService(LibroRepository libroRepository, EjemplarRepository ejemplarRepository) {
        if (libroRepository == null || ejemplarRepository == null) {
            throw new IllegalArgumentException("Los repositorios no pueden ser nulos");
        }
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
    }

    public Libro registrarLibro(String isbn, String titulo, String autor, String editorial,
                                int anio, String categoria) {
        Libro libro = new Libro(isbn, titulo, autor, editorial, anio, categoria);
        if (libroRepository.buscarPorIsbn(libro.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un libro con el ISBN " + libro.getIsbn());
        }
        return libroRepository.guardar(libro);
    }

    public Libro actualizarLibro(long id, String titulo, String autor, String editorial,
                                 int anio, String categoria) {
        Libro libro = obtenerLibro(id);
        libro.actualizar(titulo, autor, editorial, anio, categoria);
        libroRepository.actualizar(libro);
        return libro;
    }

    /** Desactivación lógica. Falla si el libro tiene ejemplares prestados. */
    public void desactivarLibro(long id) {
        Libro libro = obtenerLibro(id);
        if (!libro.isActivo()) {
            return;
        }
        boolean hayPrestados = ejemplarRepository.buscarPorLibro(id).stream()
                .anyMatch(e -> EstadoPrestado.NOMBRE.equals(e.getEstadoNombre()));
        if (hayPrestados) {
            throw new IllegalStateException("No se puede desactivar un libro con ejemplares prestados");
        }
        libroRepository.desactivar(id);
        libro.desactivar();
    }

    public Ejemplar registrarEjemplar(String codigoUnico, long libroId) {
        if (codigoUnico == null || codigoUnico.isBlank()) {
            throw new IllegalArgumentException("El código único no puede ser nulo ni vacío");
        }
        Libro libro = obtenerLibro(libroId);
        if (!libro.isActivo()) {
            throw new IllegalStateException("No se pueden registrar ejemplares de un libro inactivo");
        }
        if (ejemplarRepository.buscarPorCodigo(codigoUnico.trim()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un ejemplar con el código " + codigoUnico.trim());
        }
        return ejemplarRepository.guardar(new Ejemplar(codigoUnico, libro));
    }

    public Libro obtenerLibro(long id) {
        return libroRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el libro con id " + id));
    }

    public Optional<Libro> buscarLibroPorIsbn(String isbn) {
        return libroRepository.buscarPorIsbn(isbn);
    }

    public List<Libro> listarLibros() {
        return libroRepository.listarTodos();
    }

    public List<Libro> listarLibrosActivos() {
        return libroRepository.listarActivos();
    }

    public Ejemplar obtenerEjemplar(long id) {
        return ejemplarRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el ejemplar con id " + id));
    }

    public Optional<Ejemplar> buscarEjemplarPorCodigo(String codigoUnico) {
        return ejemplarRepository.buscarPorCodigo(codigoUnico);
    }

    public List<Ejemplar> listarEjemplaresDeLibro(long libroId) {
        return ejemplarRepository.buscarPorLibro(libroId);
    }
}
