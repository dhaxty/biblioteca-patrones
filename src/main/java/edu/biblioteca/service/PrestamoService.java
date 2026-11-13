package edu.biblioteca.service;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.Lector;
import edu.biblioteca.model.Prestamo;
import edu.biblioteca.model.PrestamoDetalle;
import edu.biblioteca.model.estado.TransicionInvalidaException;
import edu.biblioteca.repository.EjemplarRepository;
import edu.biblioteca.repository.LectorRepository;
import edu.biblioteca.repository.PrestamoRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Reglas de préstamo y devolución. Cada operación es atómica: el cambio de estado del
 * ejemplar y el registro del préstamo se guardan en la misma transacción SQLite.
 */
public class PrestamoService {

    public static final int MAX_PRESTAMOS_SIMULTANEOS = 5;

    private final Database db;
    private final LectorRepository lectores;
    private final EjemplarRepository ejemplares;
    private final PrestamoRepository prestamos;
    private final Clock reloj;

    public PrestamoService(Database db, LectorRepository lectores,
                           EjemplarRepository ejemplares, PrestamoRepository prestamos,
                           Clock reloj) {
        this.db = Objects.requireNonNull(db, "Database no puede ser nulo");
        this.lectores = Objects.requireNonNull(lectores, "LectorRepository no puede ser nulo");
        this.ejemplares = Objects.requireNonNull(ejemplares, "EjemplarRepository no puede ser nulo");
        this.prestamos = Objects.requireNonNull(prestamos, "PrestamoRepository no puede ser nulo");
        this.reloj = Objects.requireNonNull(reloj, "Clock no puede ser nulo");
    }

    public Prestamo prestar(String codigoLector, String codigoEjemplar, long operadorId) {
        return db.enTransaccion(() -> {
            LocalDate hoy = LocalDate.now(reloj);

            Lector lector = lectores.buscarPorCodigo(requerir(codigoLector, "el código del lector"))
                    .orElseThrow(() -> new ReglaNegocioException(
                            "No existe un lector con código " + codigoLector.trim()));
            if (!lector.isActivo()) {
                throw new ReglaNegocioException("El lector está desactivado");
            }
            if (prestamos.existeVencidoPorLector(lector.getId(), hoy)) {
                throw new ReglaNegocioException(
                        "El lector tiene préstamos vencidos y no puede recibir nuevos");
            }
            if (prestamos.contarActivosPorLector(lector.getId()) >= MAX_PRESTAMOS_SIMULTANEOS) {
                throw new ReglaNegocioException("El lector ya tiene "
                        + MAX_PRESTAMOS_SIMULTANEOS + " préstamos activos (máximo permitido)");
            }

            Ejemplar ejemplar = buscarEjemplar(codigoEjemplar);
            try {
                ejemplar.prestar(); // State decide si la transición es válida
            } catch (TransicionInvalidaException | IllegalStateException e) {
                throw new ReglaNegocioException(
                        "El ejemplar " + ejemplar.getCodigo() + " no se puede prestar: "
                                + e.getMessage());
            }
            ejemplares.actualizar(ejemplar);

            return prestamos.guardar(
                    Prestamo.nuevo(lector.getId(), ejemplar.getId(), operadorId, hoy));
        });
    }

    /**
     * Cierra el préstamo activo del ejemplar y lo deja disponible. Se acepta devolver con
     * atraso (no hay multas); al cerrarse el préstamo vencido, el lector se desbloquea.
     */
    public Prestamo devolver(String codigoEjemplar) {
        return db.enTransaccion(() -> {
            LocalDate hoy = LocalDate.now(reloj);
            Ejemplar ejemplar = buscarEjemplar(codigoEjemplar);
            Prestamo prestamo = prestamos.buscarActivoPorEjemplar(ejemplar.getId())
                    .orElseThrow(() -> new ReglaNegocioException(
                            "El ejemplar " + ejemplar.getCodigo() + " no tiene un préstamo activo"));
            try {
                ejemplar.devolver();
            } catch (TransicionInvalidaException | IllegalStateException e) {
                throw new ReglaNegocioException(
                        "No se puede devolver el ejemplar " + ejemplar.getCodigo() + ": "
                                + e.getMessage());
            }
            prestamo.cerrar(hoy);
            ejemplares.actualizar(ejemplar);
            prestamos.cerrar(prestamo);
            return prestamo;
        });
    }

    /**
     * Devolución con daño: pasa el ejemplar a EN_MANTENIMIENTO y cierra el préstamo.
     */
    public Prestamo devolverConDanio(String codigoEjemplar) {
        return db.enTransaccion(() -> {
            LocalDate hoy = LocalDate.now(reloj);
            Ejemplar ejemplar = buscarEjemplar(codigoEjemplar);
            Prestamo prestamo = prestamos.buscarActivoPorEjemplar(ejemplar.getId())
                    .orElseThrow(() -> new ReglaNegocioException(
                            "El ejemplar " + ejemplar.getCodigo() + " no tiene un préstamo activo"));
            try {
                ejemplar.devolverConDanio();
            } catch (TransicionInvalidaException | IllegalStateException e) {
                throw new ReglaNegocioException(
                        "No se puede devolver el ejemplar " + ejemplar.getCodigo() + ": "
                                + e.getMessage());
            }
            prestamo.cerrar(hoy);
            ejemplares.actualizar(ejemplar);
            prestamos.cerrar(prestamo);
            return prestamo;
        });
    }

    public List<PrestamoDetalle> listarActivos() {
        return prestamos.listarActivos(LocalDate.now(reloj));
    }

    public List<PrestamoDetalle> listarVencidos() {
        return prestamos.listarVencidos(LocalDate.now(reloj));
    }

    /** Para el Dashboard (DashboardService). */
    public long contarActivos() {
        return prestamos.contarActivos();
    }

    /** Para el Dashboard (DashboardService). */
    public long contarVencidos() {
        return prestamos.contarVencidos(LocalDate.now(reloj));
    }

    public Ejemplar buscarEjemplar(String codigo) {
        String c = requerir(codigo, "el código del ejemplar");
        return ejemplares.buscarPorCodigo(c)
                .orElseThrow(() -> new ReglaNegocioException("No existe un ejemplar con código " + c));
    }

    private static String requerir(String valor, String que) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("Debe indicar " + que);
        }
        return valor.trim();
    }
}
