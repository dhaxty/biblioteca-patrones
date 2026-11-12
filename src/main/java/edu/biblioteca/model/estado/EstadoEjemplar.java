package edu.biblioteca.model.estado;

import edu.biblioteca.model.Ejemplar;

/**
 * Patrón State: cada estado decide qué transiciones acepta.
 *
 * Transiciones permitidas:
 *   Disponible       -> Prestado          (prestar)
 *   Prestado         -> Disponible        (devolver, sin daño)
 *   Prestado         -> En mantenimiento  (devolver, con daño)
 *   Disponible       -> En mantenimiento  (enviarAMantenimiento)
 *   En mantenimiento -> Disponible        (habilitar)
 */
public interface EstadoEjemplar {

    TipoEstado tipo();

    void prestar(Ejemplar ejemplar);

    void devolver(Ejemplar ejemplar, boolean conDanio);

    void enviarAMantenimiento(Ejemplar ejemplar);

    void habilitar(Ejemplar ejemplar);

    /** Reconstruye el objeto de estado a partir del valor guardado en la base de datos. */
    static EstadoEjemplar desde(TipoEstado tipo) {
        return switch (tipo) {
            case DISPONIBLE -> EstadoDisponible.INSTANCIA;
            case PRESTADO -> EstadoPrestado.INSTANCIA;
            case EN_MANTENIMIENTO -> EstadoEnMantenimiento.INSTANCIA;
        };
    }
}
