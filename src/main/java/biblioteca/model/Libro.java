package biblioteca.model;

import java.util.Objects;

/** Entidad de dominio: un título del catálogo. Nunca se elimina, solo se desactiva. */
public class Libro {

    private Long id;
    private final String isbn;
    private String titulo;
    private String autor;
    private String editorial;
    private int anio;
    private String categoria;
    private boolean activo;

    /** Constructor para un libro nuevo (aún sin id, activo por defecto). */
    public Libro(String isbn, String titulo, String autor, String editorial, int anio, String categoria) {
        this(null, isbn, titulo, autor, editorial, anio, categoria, true);
    }

    /** Constructor para reconstruir un libro persistido. */
    public Libro(Long id, String isbn, String titulo, String autor, String editorial,
                 int anio, String categoria, boolean activo) {
        this.id = id;
        this.isbn = normalizarIsbn(isbn);
        this.titulo = requerirTexto(titulo, "El título");
        this.autor = requerirTexto(autor, "El autor");
        this.editorial = requerirTexto(editorial, "La editorial");
        this.anio = validarAnio(anio);
        this.categoria = requerirTexto(categoria, "La categoría");
        this.activo = activo;
    }

    /** Actualiza los datos editables. El ISBN es inmutable. */
    public void actualizar(String titulo, String autor, String editorial, int anio, String categoria) {
        String nuevoTitulo = requerirTexto(titulo, "El título");
        String nuevoAutor = requerirTexto(autor, "El autor");
        String nuevaEditorial = requerirTexto(editorial, "La editorial");
        int nuevoAnio = validarAnio(anio);
        String nuevaCategoria = requerirTexto(categoria, "La categoría");
        this.titulo = nuevoTitulo;
        this.autor = nuevoAutor;
        this.editorial = nuevaEditorial;
        this.anio = nuevoAnio;
        this.categoria = nuevaCategoria;
    }

    /** Desactivación lógica (activo = false). */
    public void desactivar() {
        this.activo = false;
    }

    private static String requerirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede ser nulo ni vacío");
        }
        return valor.trim();
    }

    private static int validarAnio(int anio) {
        if (anio <= 0) {
            throw new IllegalArgumentException("El año debe ser positivo");
        }
        return anio;
    }

    /** Quita guiones y espacios y valida el formato (ISBN-10 o ISBN-13). */
    private static String normalizarIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("El ISBN no puede ser nulo ni vacío");
        }
        String limpio = isbn.replace("-", "").replace(" ", "").toUpperCase();
        if (!limpio.matches("\\d{9}[\\dX]|\\d{13}")) {
            throw new IllegalArgumentException("Formato de ISBN inválido: " + isbn);
        }
        return limpio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public int getAnio() {
        return anio;
    }

    public String getCategoria() {
        return categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Libro otro && isbn.equals(otro.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return "Libro{id=" + id + ", isbn=" + isbn + ", titulo=" + titulo + ", activo=" + activo + "}";
    }
}
