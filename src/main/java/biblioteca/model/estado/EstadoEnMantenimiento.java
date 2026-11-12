package biblioteca.model.estado;

public record EstadoEnMantenimiento() implements EstadoEjemplar {

    public static final String NOMBRE = "EN_MANTENIMIENTO";

    @Override
    public String nombre() {
        return NOMBRE;
    }

    @Override
    public EstadoEjemplar prestar() {
        throw new IllegalStateException("Un ejemplar en mantenimiento no puede prestarse");
    }

    @Override
    public EstadoEjemplar devolver() {
        throw new IllegalStateException("Un ejemplar en mantenimiento no puede devolverse");
    }

    @Override
    public EstadoEjemplar enviarAMantenimiento() {
        throw new IllegalStateException("El ejemplar ya está en mantenimiento");
    }

    @Override
    public EstadoEjemplar habilitar() {
        return new EstadoDisponible();
    }
}
