package edu.biblioteca.model;

import java.time.LocalDateTime;

/**
 * Proyección de lectura (DTO) para mostrar el historial de mantenimiento
 * con el código del ejemplar asociado, optimizando la consulta en una sola operación.
 */
public record RegistroMantenimientoDetalle(
        String codigoEjemplar,
        String motivo,
        LocalDateTime ingreso,
        LocalDateTime habilitacion
) {}
