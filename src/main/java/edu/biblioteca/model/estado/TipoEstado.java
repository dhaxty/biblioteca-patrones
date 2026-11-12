package edu.biblioteca.model.estado;

/** Identificador persistible de cada estado de un ejemplar (se guarda como texto en SQLite). */
public enum TipoEstado {
    DISPONIBLE("Disponible"),
    PRESTADO("Prestado"),
    EN_MANTENIMIENTO("En mantenimiento");

    private final String etiqueta;

    TipoEstado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto legible para mostrar en las vistas. */
    public String etiqueta() {
        return etiqueta;
    }
}
