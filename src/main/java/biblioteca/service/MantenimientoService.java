package biblioteca.service;

import biblioteca.model.Ejemplar;
import biblioteca.model.RegistroMantenimiento;
import biblioteca.repository.EjemplarRepository;
import biblioteca.repository.MantenimientoRepository;
import biblioteca.repository.RepositoryException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

/**
 * Retiro e ingreso a mantenimiento con transacciones explícitas.
 * Los repositorios recibidos deben usar la MISMA {@link Connection} que este servicio.
 */
public class MantenimientoService {

    private final Connection connection;
    private final EjemplarRepository ejemplarRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final Clock reloj;

    public MantenimientoService(Connection connection,
                                EjemplarRepository ejemplarRepository,
                                MantenimientoRepository mantenimientoRepository) {
        this(connection, ejemplarRepository, mantenimientoRepository, Clock.systemDefaultZone());
    }

    public MantenimientoService(Connection connection,
                                EjemplarRepository ejemplarRepository,
                                MantenimientoRepository mantenimientoRepository,
                                Clock reloj) {
        if (connection == null || ejemplarRepository == null || mantenimientoRepository == null || reloj == null) {
            throw new IllegalArgumentException("Las dependencias no pueden ser nulas");
        }
        this.connection = connection;
        this.ejemplarRepository = ejemplarRepository;
        this.mantenimientoRepository = mantenimientoRepository;
        this.reloj = reloj;
    }

    /** Retira un ejemplar disponible y abre su registro de mantenimiento. */
    public RegistroMantenimiento enviarAMantenimiento(long ejemplarId, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo no puede ser nulo ni vacío");
        }
        return enTransaccion(() -> {
            Ejemplar ejemplar = obtenerEjemplar(ejemplarId);
            ejemplar.enviarAMantenimiento(); // IllegalStateException si no está DISPONIBLE
            ejemplarRepository.actualizar(ejemplar);
            RegistroMantenimiento registro =
                    new RegistroMantenimiento(ejemplarId, motivo, LocalDateTime.now(reloj));
            return mantenimientoRepository.guardar(registro);
        });
    }

    /** Habilita un ejemplar en mantenimiento y cierra su registro con la fecha de habilitación. */
    public RegistroMantenimiento habilitar(long ejemplarId) {
        return enTransaccion(() -> {
            Ejemplar ejemplar = obtenerEjemplar(ejemplarId);
            ejemplar.habilitar(); // IllegalStateException si no está EN_MANTENIMIENTO
            RegistroMantenimiento registro = mantenimientoRepository.buscarAbiertoPorEjemplar(ejemplarId)
                    .orElseThrow(() -> new IllegalStateException(
                            "El ejemplar " + ejemplarId + " no tiene un registro de mantenimiento abierto"));
            registro.finalizar(LocalDateTime.now(reloj));
            mantenimientoRepository.actualizar(registro);
            ejemplarRepository.actualizar(ejemplar);
            return registro;
        });
    }

    public List<RegistroMantenimiento> historial(long ejemplarId) {
        return mantenimientoRepository.listarPorEjemplar(ejemplarId);
    }

    private Ejemplar obtenerEjemplar(long ejemplarId) {
        return ejemplarRepository.buscarPorId(ejemplarId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el ejemplar con id " + ejemplarId));
    }

    private <T> T enTransaccion(Supplier<T> trabajo) {
        boolean autoCommitPrevio;
        try {
            autoCommitPrevio = connection.getAutoCommit();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo iniciar la transacción", e);
        }
        try {
            T resultado = trabajo.get();
            connection.commit();
            return resultado;
        } catch (RuntimeException e) {
            revertir(e);
            throw e;
        } catch (SQLException e) {
            revertir(e);
            throw new RepositoryException("No se pudo confirmar la transacción", e);
        } finally {
            try {
                connection.setAutoCommit(autoCommitPrevio);
            } catch (SQLException ignorada) {
                // la conexión ya falló; no hay más que hacer
            }
        }
    }

    private void revertir(Exception causa) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            causa.addSuppressed(e);
        }
    }
}
