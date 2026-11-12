package edu.biblioteca.service;

import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.Libro;
import edu.biblioteca.model.estado.TipoEstado;
import edu.biblioteca.repository.EjemplarRepository;
import edu.biblioteca.repository.LibroRepository;

import java.util.List;
import java.util.Optional;

/** Reglas del catálogo: libros y sus ejemplares físicos. */
public class CatalogoService {

    private final LibroRepository libros;
    private final EjemplarRepository ejemplares;

    public CatalogoService(LibroRepository libros, EjemplarRepository ejemplares) {
        this.libros = libros;
        this.ejemplares = ejemplares;
    }

    public Libro registrarLibro(String isbn, String titulo, String autor,
                                String editorial, int anio, String categoria) {
        Libro libro = convertirErrores(() -> new Libro(isbn, titulo, autor, editorial, anio, categoria));
        if (libros.buscarPorIsbn(libro.getIsbn()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un libro con el ISBN " + libro.getIsbn() + ".");
        }
        return libros.guardar(libro);
    }

    public Libro editarLibro(long libroId, String titulo, String autor,
                             String editorial, int anio, String categoria) {
        Libro libro = obtenerLibro(libroId);
        convertirErrores(() -> {
            libro.actualizarDatos(titulo, autor, editorial, anio, categoria);
            return null;
        });
        libros.actualizar(libro);
        return libro;
    }

    public Ejemplar agregarEjemplar(long libroId, String codigo) {
        Libro libro = obtenerLibro(libroId);
        if (!libro.isActivo()) {
            throw new ReglaNegocioException("No se pueden agregar ejemplares a un libro desactivado.");
        }
        Ejemplar ejemplar = convertirErrores(() -> new Ejemplar(codigo, libroId));
        if (ejemplares.buscarPorCodigo(ejemplar.getCodigo()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un ejemplar con el código " + ejemplar.getCodigo() + ".");
        }
        return ejemplares.guardar(ejemplar);
    }

    /** Busca libros activos por título, autor, ISBN o código de ejemplar. Texto vacío = todos. */
    public List<Libro> buscarLibros(String texto) {
        return libros.buscar(texto == null ? "" : texto.trim(), true);
    }

    public Libro obtenerLibro(long libroId) {
        return libros.buscarPorId(libroId)
                .orElseThrow(() -> new ReglaNegocioException("No existe el libro solicitado."));
    }

    public List<Ejemplar> listarEjemplares(long libroId) {
        return ejemplares.listarPorLibro(libroId);
    }

    public Optional<Ejemplar> buscarEjemplarPorCodigo(String codigo) {
        return ejemplares.buscarPorCodigo(codigo == null ? "" : codigo.trim());
    }

    /** Desactivación lógica: el historial se conserva. No se permite con ejemplares prestados. */
    public void desactivarLibro(long libroId) {
        Libro libro = obtenerLibro(libroId);
        boolean hayPrestados = ejemplares.listarPorLibro(libroId).stream()
                .anyMatch(e -> e.getTipoEstado() == TipoEstado.PRESTADO);
        if (hayPrestados) {
            throw new ReglaNegocioException(
                    "No se puede desactivar el libro: tiene ejemplares prestados pendientes de devolución.");
        }
        libro.desactivar();
        libros.actualizar(libro);
    }

    private static <T> T convertirErrores(java.util.function.Supplier<T> accion) {
        try {
            return accion.get();
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException(e.getMessage());
        }
    }
}
