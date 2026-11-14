package edu.biblioteca.repository.jdbc;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.RegistroMantenimiento;
import edu.biblioteca.model.RegistroMantenimientoDetalle;
import edu.biblioteca.repository.MantenimientoRepository;
import edu.biblioteca.repository.RepositorioException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMantenimientoRepository implements MantenimientoRepository {

    private static final String COLUMNAS = "id, ejemplar_id, motivo, ingreso, habilitacion";

    private final Database db;

    public JdbcMantenimientoRepository(Database db) {
        this.db = db;
    }

    @Override
    public RegistroMantenimiento abrir(RegistroMantenimiento registro) {
        String sql = "INSERT INTO registro_mantenimiento (ejemplar_id, motivo, ingreso, habilitacion) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            ps.setLong(1, registro.getEjemplarId());
            ps.setString(2, registro.getMotivo());
            ps.setString(3, registro.getIngreso().toString());
            ps.setString(4, registro.getHabilitacion() == null ? null : registro.getHabilitacion().toString());
            ps.executeUpdate();
            registro.asignarId(db.ultimoIdInsertado());
            return registro;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo registrar el ingreso a mantenimiento.", e);
        }
    }

    @Override
    public void finalizar(RegistroMantenimiento registro) {
        try (PreparedStatement ps = db.conexion()
                .prepareStatement("UPDATE registro_mantenimiento SET habilitacion = ? WHERE id = ?")) {
            ps.setString(1, registro.getHabilitacion().toString());
            ps.setLong(2, registro.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo finalizar el mantenimiento.", e);
        }
    }

    @Override
    public Optional<RegistroMantenimiento> buscarAbiertoPorEjemplar(long ejemplarId) {
        List<RegistroMantenimiento> resultado = consultar("SELECT " + COLUMNAS
                + " FROM registro_mantenimiento WHERE ejemplar_id = ? AND habilitacion IS NULL "
                + "ORDER BY id DESC LIMIT 1", ejemplarId);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<RegistroMantenimiento> listarPorEjemplar(long ejemplarId) {
        return consultar("SELECT " + COLUMNAS
                + " FROM registro_mantenimiento WHERE ejemplar_id = ? ORDER BY id DESC", ejemplarId);
    }

    @Override
    public List<RegistroMantenimientoDetalle> listarHistorialCompleto() {
        String sql = """
                SELECT e.codigo, r.motivo, r.ingreso, r.habilitacion
                FROM registro_mantenimiento r
                JOIN ejemplar e ON r.ejemplar_id = e.id
                ORDER BY r.id DESC
                """;
        try (PreparedStatement ps = db.conexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RegistroMantenimientoDetalle> lista = new ArrayList<>();
            while (rs.next()) {
                String hab = rs.getString("habilitacion");
                lista.add(new RegistroMantenimientoDetalle(
                        rs.getString("codigo"),
                        rs.getString("motivo"),
                        LocalDateTime.parse(rs.getString("ingreso")),
                        hab == null ? null : LocalDateTime.parse(hab)
                ));
            }
            return lista;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo consultar el historial completo de mantenimiento.", e);
        }
    }

    private List<RegistroMantenimiento> consultar(String sql, Object parametro) {
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                List<RegistroMantenimiento> registros = new ArrayList<>();
                while (rs.next()) {
                    String habilitacion = rs.getString("habilitacion");
                    registros.add(new RegistroMantenimiento(
                            rs.getLong("id"),
                            rs.getLong("ejemplar_id"),
                            rs.getString("motivo"),
                            LocalDateTime.parse(rs.getString("ingreso")),
                            habilitacion == null ? null : LocalDateTime.parse(habilitacion)));
                }
                return registros;
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo consultar el mantenimiento.", e);
        }
    }
}
