package biblioteca.repository.jdbc;

import biblioteca.model.RegistroMantenimiento;
import biblioteca.repository.MantenimientoRepository;
import biblioteca.repository.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MantenimientoRepositoryJdbc implements MantenimientoRepository {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String COLUMNAS = "id, ejemplar_id, motivo, ingreso, habilitacion";

    private final Connection connection;

    public MantenimientoRepositoryJdbc(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula");
        }
        this.connection = connection;
    }

    @Override
    public RegistroMantenimiento guardar(RegistroMantenimiento registro) {
        String sql = "INSERT INTO registro_mantenimiento (ejemplar_id, motivo, ingreso, habilitacion) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, registro.getEjemplarId());
            ps.setString(2, registro.getMotivo());
            ps.setString(3, formatear(registro.getIngreso()));
            ps.setString(4, formatear(registro.getHabilitacion()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    registro.setId(keys.getLong(1));
                }
            }
            return registro;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo guardar el registro de mantenimiento", e);
        }
    }

    @Override
    public void actualizar(RegistroMantenimiento registro) {
        if (registro.getId() == null) {
            throw new IllegalArgumentException("El registro no tiene id; guárdalo primero");
        }
        String sql = "UPDATE registro_mantenimiento SET habilitacion = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, formatear(registro.getHabilitacion()));
            ps.setLong(2, registro.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe el registro con id " + registro.getId());
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo actualizar el registro " + registro.getId(), e);
        }
    }

    @Override
    public Optional<RegistroMantenimiento> buscarPorId(long id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT " + COLUMNAS + " FROM registro_mantenimiento WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo buscar el registro " + id, e);
        }
    }

    @Override
    public Optional<RegistroMantenimiento> buscarAbiertoPorEjemplar(long ejemplarId) {
        String sql = "SELECT " + COLUMNAS + " FROM registro_mantenimiento "
                + "WHERE ejemplar_id = ? AND habilitacion IS NULL ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ejemplarId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo buscar el mantenimiento abierto del ejemplar " + ejemplarId, e);
        }
    }

    @Override
    public List<RegistroMantenimiento> listarPorEjemplar(long ejemplarId) {
        String sql = "SELECT " + COLUMNAS + " FROM registro_mantenimiento WHERE ejemplar_id = ? ORDER BY id";
        List<RegistroMantenimiento> resultado = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ejemplarId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo listar el historial del ejemplar " + ejemplarId, e);
        }
    }

    private static String formatear(LocalDateTime fecha) {
        return fecha == null ? null : ISO.format(fecha);
    }

    private static LocalDateTime parsear(String texto) {
        return texto == null ? null : LocalDateTime.parse(texto, ISO);
    }

    private static RegistroMantenimiento mapear(ResultSet rs) throws SQLException {
        return new RegistroMantenimiento(
                rs.getLong("id"),
                rs.getLong("ejemplar_id"),
                rs.getString("motivo"),
                parsear(rs.getString("ingreso")),
                parsear(rs.getString("habilitacion")));
    }
}
