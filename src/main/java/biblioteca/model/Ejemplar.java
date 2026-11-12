package biblioteca.model;

import biblioteca.model.estado.EstadoDisponible;
import biblioteca.model.estado.EstadoEjemplar;

/**
 * Copia física de un {@link Libro}. Delega todas las transiciones al patrón State;
 * una transición inválida lanza {@link IllegalStateException}.
 */
public class Ejemplar {

    private Long id;
    private final String codigoUnico;
    private final Libro libro;
    private EstadoEjemplar estado;

    /** Ejemplar nuevo: nace DISPONIBLE. */
    public Ejemplar(String codigoUnico, Libro libro) {
        this(null, codigoUnico, libro, new EstadoDisponible());
    }

    /** Reconstruye un ejemplar persistido. */
    public Ejemplar(Long id, String codigoUnico, Libro libro, EstadoEjemplar estado) {
        if (codigoUnico == null || codigoUnico.isBlank()) {
            throw new IllegalArgumentException("El código único no puede ser nulo ni vacío");
        }
        if (libro == null) {
            throw new IllegalArgumentException("El libro no puede ser nulo");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        this.id = id;
        this.codigoUnico = codigoUnico.trim();
        this.libro = libro;
        this.estado = estado;
    }

    public void prestar() {
        estado = estado.prestar();
    }

    public void devolver() {
        estado = estado.devolver();
    }

    public void enviarAMantenimiento() {
        estado = estado.enviarAMantenimiento();
    }

    public void habilitar() {
        estado = estado.habilitar();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoUnico() {
        return codigoUnico;
    }

    public Libro getLibro() {
        return libro;
    }

    public EstadoEjemplar getEstado() {
        return estado;
    }

    public String getEstadoNombre() {
        return estado.nombre();
    }

    @Override
    public String toString() {
        return "Ejemplar{id=" + id + ", codigo=" + codigoUnico + ", estado=" + estado.nombre() + "}";
    }
}
