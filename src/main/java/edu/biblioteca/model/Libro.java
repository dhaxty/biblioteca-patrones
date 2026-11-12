package edu.biblioteca.model;

import java.time.Year;

/** Título del catálogo. Se desactiva lógicamente: nunca se borra, para conservar el historial. */
public class Libro {

    private Long id;
    private final String isbn;
    private String titulo;
    private String autor;
    private String editorial;
    private int anio;
    private String categoria;
    private boolean activo;

    /** Constructor para un libro nuevo (aún sin id, activo). */
    public Libro(String isbn, String titulo, String autor, String editorial, int anio, String categoria) {
        this(null, isbn, titulo, autor, editorial, anio, categoria, true);
    }

    /** Constructor para reconstruir un libro desde la base de datos. */
    public Libro(Long id, String isbn, String titulo, String autor, String editorial,
                 int anio, String categoria, boolean activo) {
        this.id = id;
        this.isbn = requerido(isbn, "El ISBN");
        this.activo = activo;
        asignarDatos(titulo, autor, editorial, anio, categoria);
    }

    public void actualizarDatos(String titulo, String autor, String editorial, int anio, String categoria) {
        asignarDatos(titulo, autor, editorial, anio, categoria);
    }

    public void desactivar() {
        this.activo = false;
    }

    private void asignarDatos(String titulo, String autor, String editorial, int anio, String categoria) {
        this.titulo = requerido(titulo, "El título");
        this.autor = requerido(autor, "El autor");
        if (anio < 1000 || anio > Year.now().getValue()) {
            throw new IllegalArgumentException("El año de publicación no es válido: " + anio);
        }
        this.anio = anio;
        this.editorial = editorial == null ? "" : editorial.trim();
        this.categoria = categoria == null ? "" : categoria.trim();
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.trim();
    }

    /** Lo usa el repositorio al insertar. Solo se puede asignar una vez. */
    public void asignarId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("El libro ya tiene id.");
        }
        this.id = id;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getEditorial() { return editorial; }
    public int getAnio() { return anio; }
    public String getCategoria() { return categoria; }
    public boolean isActivo() { return activo; }

    @Override
    public String toString() {
        return titulo + " (" + autor + ")";
    }
}
