package biblioteca.model.estado;

public record EstadoPrestado() implements EstadoEjemplar {

    public static final String NOMBRE = "PRESTADO";

    @Override
    public String nombre() {
        return NOMBRE;
    }

    @Override
    public EstadoEjemplar prestar() {
        throw new IllegalStateException("El ejemplar ya está prestado");
    }

    @Override
    public EstadoEjemplar devolver() {
        return new EstadoDisponible();
    }

    @Override
    public EstadoEjemplar enviarAMantenimiento() {
        throw new IllegalStateException("Un ejemplar prestado debe devolverse antes de enviarlo a mantenimiento");
    }

    @Override
    public EstadoEjemplar habilitar() {
        throw new IllegalStateException("Un ejemplar prestado no está en mantenimiento");
    }
}
