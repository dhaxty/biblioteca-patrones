package edu.biblioteca.model;

import edu.biblioteca.model.estado.EstadoEjemplar;
import edu.biblioteca.model.estado.TipoEstado;

import java.util.Objects;

/** Copia física de un libro. Su comportamiento depende del estado actual (patrón State). */
public class Ejemplar {

    private Long id;
    private final String codigo;
    private final long libroId;
    private EstadoEjemplar estado;

    /** Ejemplar nuevo: siempre nace Disponible. */
    public Ejemplar(String codigo, long libroId) {
        this(null, codigo, libroId, TipoEstado.DISPONIBLE);
    }

    /** Reconstruye un ejemplar desde la base de datos. */
    public Ejemplar(Long id, String codigo, long libroId, TipoEstado tipoEstado) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del ejemplar es obligatorio.");
        }
        this.id = id;
        this.codigo = codigo.trim();
        this.libroId = libroId;
        this.estado = EstadoEjemplar.desde(Objects.requireNonNull(tipoEstado));
    }

    // --- Comportamiento delegado al estado ---

    /** Disponible -> Prestado. */
    public void prestar() {
        estado.prestar(this);
    }

    /** Prestado -> Disponible. */
    public void devolver() {
        estado.devolver(this, false);
    }

    /** Prestado -> En mantenimiento (devolución dañada). */
    public void devolverConDanio() {
        estado.devolver(this, true);
    }

    /** Disponible -> En mantenimiento (retiro). */
    public void enviarAMantenimiento() {
        estado.enviarAMantenimiento(this);
    }

    /** En mantenimiento -> Disponible. */
    public void habilitar() {
        estado.habilitar(this);
    }

    /** Solo deben invocarlo las clases de {@code model.estado}. */
    public void cambiarEstado(EstadoEjemplar nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado);
    }

    public boolean estaDisponible() {
        return estado.tipo() == TipoEstado.DISPONIBLE;
    }

    /** Lo usa el repositorio al insertar. Solo se puede asignar una vez. */
    public void asignarId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("El ejemplar ya tiene id.");
        }
        this.id = id;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public long getLibroId() { return libroId; }
    public EstadoEjemplar getEstado() { return estado; }
    public TipoEstado getTipoEstado() { return estado.tipo(); }

    @Override
    public String toString() {
        return codigo + " [" + estado.tipo().etiqueta() + "]";
    }
}
