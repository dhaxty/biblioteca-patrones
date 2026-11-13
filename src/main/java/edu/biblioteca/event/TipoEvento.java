package edu.biblioteca.event;

public enum TipoEvento {
    /** Se registró o desactivó un libro, o se agregó un ejemplar. */
    CATALOGO_CAMBIO,
    /** Se registró o desactivó un lector. */
    LECTORES_CAMBIO,
    /** Se registró un préstamo o una devolución. */
    PRESTAMOS_CAMBIO,
    /** Un ejemplar entró o salió de mantenimiento. */
    MANTENIMIENTO_CAMBIO,
    /** Se creó o desactivó una cuenta del personal. */
    CUENTAS_CAMBIO
}
