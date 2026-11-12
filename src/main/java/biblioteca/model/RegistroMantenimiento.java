package biblioteca.model;

import java.time.LocalDateTime;

/** Historial de mantenimiento: ingreso y fecha de habilitación (nula mientras siga abierto). */
public class RegistroMantenimiento {

    private Long id;
    private final long ejemplarId;
    private final String motivo;
    private final LocalDateTime ingreso;
    private LocalDateTime habilitacion;

    /** Registro nuevo y abierto. */
    public RegistroMantenimiento(long ejemplarId, String motivo, LocalDateTime ingreso) {
        this(null, ejemplarId, motivo, ingreso, null);
    }

    /** Reconstruye un registro persistido. */
    public RegistroMantenimiento(Long id, long ejemplarId, String motivo,
                                 LocalDateTime ingreso, LocalDateTime habilitacion) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo no puede ser nulo ni vacío");
        }
        if (ingreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser nula");
        }
        if (habilitacion != null && habilitacion.isBefore(ingreso)) {
            throw new IllegalArgumentException("La habilitación no puede ser anterior al ingreso");
        }
        this.id = id;
        this.ejemplarId = ejemplarId;
        this.motivo = motivo.trim();
        this.ingreso = ingreso;
        this.habilitacion = habilitacion;
    }

    /** Cierra el registro con la fecha de habilitación. */
    public void finalizar(LocalDateTime fechaHabilitacion) {
        if (habilitacion != null) {
            throw new IllegalStateException("El registro de mantenimiento ya fue finalizado");
        }
        if (fechaHabilitacion == null) {
            throw new IllegalArgumentException("La fecha de habilitación no puede ser nula");
        }
        if (fechaHabilitacion.isBefore(ingreso)) {
            throw new IllegalArgumentException("La habilitación no puede ser anterior al ingreso");
        }
        this.habilitacion = fechaHabilitacion;
    }

    public boolean estaAbierto() {
        return habilitacion == null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getEjemplarId() {
        return ejemplarId;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getIngreso() {
        return ingreso;
    }

    public LocalDateTime getHabilitacion() {
        return habilitacion;
    }
}
