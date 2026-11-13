package edu.biblioteca.service;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.Prestamo;
import edu.biblioteca.model.PrestamoDetalle;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Coordina PrestamoService y MantenimientoService en una sola transacción y avisa a los
 * observadores únicamente después del COMMIT (si algo falla, no se publica nada).
 */
public class BibliotecaFacade implements BibliotecaOperaciones {

    public static final String EVENTO_PRESTAMO_REGISTRADO = "PRESTAMO_REGISTRADO";
    public static final String EVENTO_DEVOLUCION_REGISTRADA = "DEVOLUCION_REGISTRADA";
    public static final String EVENTO_MANTENIMIENTO_ACTUALIZADO = "MANTENIMIENTO_ACTUALIZADO";

    private final Database db;
    private final PrestamoService prestamos;
    private final MantenimientoService mantenimiento;
    private final Consumer<String> eventos;

    public BibliotecaFacade(Database db, PrestamoService prestamos,
                            MantenimientoService mantenimiento, Consumer<String> eventos) {
        this.db = Objects.requireNonNull(db, "Database no puede ser nulo");
        this.prestamos = Objects.requireNonNull(prestamos, "PrestamoService no puede ser nulo");
        this.mantenimiento = Objects.requireNonNull(mantenimiento, "MantenimientoService no puede ser nulo");
        this.eventos = eventos;
    }

    @Override
    public Prestamo prestar(String codigoLector, String codigoEjemplar, long operadorId) {
        Prestamo prestamo = db.enTransaccion(
                () -> prestamos.prestar(codigoLector, codigoEjemplar, operadorId));
        notificar(EVENTO_PRESTAMO_REGISTRADO);
        return prestamo;
    }

    @Override
    public Prestamo devolver(String codigoEjemplar, boolean danado, String motivoDanio) {
        if (danado && (motivoDanio == null || motivoDanio.isBlank())) {
            throw new ReglaNegocioException("Debe indicar el motivo del daño");
        }
        Prestamo prestamo = db.enTransaccion(() -> {
            Prestamo cerrado;
            if (danado) {
                cerrado = prestamos.devolverConDanio(codigoEjemplar);
                mantenimiento.registrarIngresoPorDanio(cerrado.getEjemplarId(), motivoDanio.trim());
            } else {
                cerrado = prestamos.devolver(codigoEjemplar);
            }
            return cerrado;
        });
        notificar(EVENTO_DEVOLUCION_REGISTRADA);
        if (danado) {
            notificar(EVENTO_MANTENIMIENTO_ACTUALIZADO);
        }
        return prestamo;
    }

    @Override
    public void retirarEjemplarAMantenimiento(String codigoEjemplar, String motivo) {
        db.enTransaccion(() -> {
            Ejemplar ejemplar = prestamos.buscarEjemplar(codigoEjemplar);
            mantenimiento.retirarEjemplar(ejemplar.getId(), motivo);
        });
        notificar(EVENTO_MANTENIMIENTO_ACTUALIZADO);
    }

    @Override
    public void habilitarEjemplar(String codigoEjemplar) {
        db.enTransaccion(() -> {
            Ejemplar ejemplar = prestamos.buscarEjemplar(codigoEjemplar);
            mantenimiento.habilitarEjemplar(ejemplar.getId());
        });
        notificar(EVENTO_MANTENIMIENTO_ACTUALIZADO);
    }

    @Override
    public List<PrestamoDetalle> listarActivos() {
        return prestamos.listarActivos();
    }

    @Override
    public List<PrestamoDetalle> listarVencidos() {
        return prestamos.listarVencidos();
    }

    private void notificar(String evento) {
        if (eventos != null) {
            eventos.accept(evento);
        }
    }
}
