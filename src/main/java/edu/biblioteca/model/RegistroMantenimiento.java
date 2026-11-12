package edu.biblioteca.model;

import java.time.LocalDateTime;

/** Historial de una estadía de un ejemplar en mantenimiento. Abierto mientras no tenga fecha de habilitación. */
public class RegistroMantenimiento {

    private Long id;
    private final long ejemplarId;
    private final String motivo;
    private final LocalDateTime ingreso;
    private LocalDateTime habilitacion;

    public RegistroMantenimiento(long ejemplarId, String motivo, LocalDateTime ingreso) {
        this(null, ejemplarId, motivo, ingreso, null);
    }

    public RegistroMantenimiento(Long id, long ejemplarId, String motivo,
                                 LocalDateTime ingreso, LocalDateTime habilitacion) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de mantenimiento es obligatorio.");
        }
        if (ingreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria.");
        }
        this.id = id;
        this.ejemplarId = ejemplarId;
        this.motivo = motivo.trim();
        this.ingreso = ingreso;
        this.habilitacion = habilitacion;
    }

    public void finalizar(LocalDateTime fechaHabilitacion) {
        if (habilitacion != null) {
            throw new IllegalStateException("El registro de mantenimiento ya fue finalizado.");
        }
        if (fechaHabilitacion.isBefore(ingreso)) {
            throw new IllegalArgumentException("La habilitación no puede ser anterior al ingreso.");
        }
        this.habilitacion = fechaHabilitacion;
    }

    public boolean estaAbierto() {
        return habilitacion == null;
    }

    public void asignarId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("El registro ya tiene id.");
        }
        this.id = id;
    }

    public Long getId() { return id; }
    public long getEjemplarId() { return ejemplarId; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getIngreso() { return ingreso; }
    public LocalDateTime getHabilitacion() { return habilitacion; }
}
