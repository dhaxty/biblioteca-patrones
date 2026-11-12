package biblioteca.model.estado;

public record EstadoDisponible() implements EstadoEjemplar {

    public static final String NOMBRE = "DISPONIBLE";

    @Override
    public String nombre() {
        return NOMBRE;
    }

    @Override
    public EstadoEjemplar prestar() {
        return new EstadoPrestado();
    }

    @Override
    public EstadoEjemplar devolver() {
        throw new IllegalStateException("Un ejemplar disponible no puede devolverse");
    }

    @Override
    public EstadoEjemplar enviarAMantenimiento() {
        return new EstadoEnMantenimiento();
    }

    @Override
    public EstadoEjemplar habilitar() {
        throw new IllegalStateException("Un ejemplar disponible no está en mantenimiento");
    }
}
