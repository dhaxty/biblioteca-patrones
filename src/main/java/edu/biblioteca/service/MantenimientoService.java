package edu.biblioteca.service;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.RegistroMantenimiento;
import edu.biblioteca.model.estado.TipoEstado;
import edu.biblioteca.model.estado.TransicionInvalidaException;
import edu.biblioteca.repository.EjemplarRepository;
import edu.biblioteca.repository.MantenimientoRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Retiro y habilitación de ejemplares. Cada operación guarda estado + historial en una sola transacción. */
public class MantenimientoService {

    private final Database db;
    private final EjemplarRepository ejemplares;
    private final MantenimientoRepository mantenimientos;

    public MantenimientoService(Database db, EjemplarRepository ejemplares, MantenimientoRepository mantenimientos) {
        this.db = db;
        this.ejemplares = ejemplares;
        this.mantenimientos = mantenimientos;
    }

    /** Disponible -> En mantenimiento, con motivo obligatorio. */
    public RegistroMantenimiento retirarEjemplar(long ejemplarId, String motivo) {
        exigirMotivo(motivo);
        return db.enTransaccion(() -> {
            Ejemplar ejemplar = obtenerEjemplar(ejemplarId);
            try {
                ejemplar.enviarAMantenimiento();
            } catch (TransicionInvalidaException e) {
                throw new ReglaNegocioException(e.getMessage());
            }
            ejemplares.actualizar(ejemplar);
            return mantenimientos.abrir(new RegistroMantenimiento(ejemplarId, motivo, LocalDateTime.now()));
        });
    }

    /** En mantenimiento -> Disponible; cierra el registro abierto con la fecha de habilitación. */
    public RegistroMantenimiento habilitarEjemplar(long ejemplarId) {
        return db.enTransaccion(() -> {
            Ejemplar ejemplar = obtenerEjemplar(ejemplarId);
            try {
                ejemplar.habilitar();
            } catch (TransicionInvalidaException e) {
                throw new ReglaNegocioException(e.getMessage());
            }
            RegistroMantenimiento registro = mantenimientos.buscarAbiertoPorEjemplar(ejemplarId)
                    .orElseThrow(() -> new ReglaNegocioException(
                            "El ejemplar no tiene un registro de mantenimiento abierto."));
            registro.finalizar(LocalDateTime.now());
            ejemplares.actualizar(ejemplar);
            mantenimientos.finalizar(registro);
            return registro;
        });
    }

    /**
     * Para el módulo de préstamos (B): en una devolución dañada, el Facade llama primero a
     * {@code ejemplar.devolverConDanio()} + {@code ejemplares.actualizar(...)} y luego a este método
     * para dejar el registro de mantenimiento, todo dentro de su misma transacción.
     */
    public RegistroMantenimiento registrarIngresoPorDanio(long ejemplarId, String motivo) {
        exigirMotivo(motivo);
        return db.enTransaccion(() -> {
            Ejemplar ejemplar = obtenerEjemplar(ejemplarId);
            if (ejemplar.getTipoEstado() != TipoEstado.EN_MANTENIMIENTO) {
                throw new ReglaNegocioException("El ejemplar debe estar en mantenimiento para registrar el daño.");
            }
            if (mantenimientos.buscarAbiertoPorEjemplar(ejemplarId).isPresent()) {
                throw new ReglaNegocioException("El ejemplar ya tiene un mantenimiento abierto.");
            }
            return mantenimientos.abrir(new RegistroMantenimiento(ejemplarId, motivo, LocalDateTime.now()));
        });
    }

    public List<RegistroMantenimiento> consultarHistorial(long ejemplarId) {
        return mantenimientos.listarPorEjemplar(ejemplarId);
    }

    /** Ejemplares actualmente fuera de circulación (para el panel de mantenimiento). */
    public List<Ejemplar> listarEjemplaresEnMantenimiento() {
        return ejemplares.listarPorEstado(TipoEstado.EN_MANTENIMIENTO);
    }

    private Ejemplar obtenerEjemplar(long ejemplarId) {
        return ejemplares.buscarPorId(ejemplarId)
                .orElseThrow(() -> new ReglaNegocioException("No existe el ejemplar solicitado."));
    }

    private static void exigirMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("El motivo de mantenimiento es obligatorio.");
        }
    }
}
