package biblioteca.model.estado;

/**
 * Patrón GoF State para el ciclo de vida de un {@code Ejemplar}.
 * Cada operación devuelve el nuevo estado, o lanza {@link IllegalStateException}
 * si la transición no está permitida desde el estado actual.
 */
public interface EstadoEjemplar {

    /** Nombre persistido en la base de datos (DISPONIBLE, PRESTADO, EN_MANTENIMIENTO). */
    String nombre();

    EstadoEjemplar prestar();

    EstadoEjemplar devolver();

    EstadoEjemplar enviarAMantenimiento();

    EstadoEjemplar habilitar();

    /** Reconstruye el estado a partir del nombre guardado en la base de datos. */
    static EstadoEjemplar desdeNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del estado no puede ser nulo ni vacío");
        }
        return switch (nombre) {
            case EstadoDisponible.NOMBRE -> new EstadoDisponible();
            case EstadoPrestado.NOMBRE -> new EstadoPrestado();
            case EstadoEnMantenimiento.NOMBRE -> new EstadoEnMantenimiento();
            default -> throw new IllegalArgumentException("Estado desconocido: " + nombre);
        };
    }
}
