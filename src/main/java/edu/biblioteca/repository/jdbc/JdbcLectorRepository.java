package edu.biblioteca.repository.jdbc;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Lector;
import edu.biblioteca.model.TipoLector;
import edu.biblioteca.repository.LectorRepository;
import edu.biblioteca.repository.RepositorioException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLectorRepository implements LectorRepository {

    private static final String COLUMNAS = "id, codigo_universitario, nombre, correo, tipo, activo";

    private final Database db;

    public JdbcLectorRepository(Database db) {
        this.db = db;
    }

    @Override
    public Lector guardar(Lector lector) {
        String sql = "INSERT INTO lector (codigo_universitario, nombre, correo, tipo, activo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            ps.setString(1, lector.getCodigoUniversitario());
            ps.setString(2, lector.getNombre());
            ps.setString(3, lector.getCorreo());
            ps.setString(4, lector.getTipo().name());
            ps.setInt(5, lector.isActivo() ? 1 : 0);
            ps.executeUpdate();
            lector.asignarId(db.ultimoIdInsertado());
            return lector;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo guardar el lector.", e);
        }
    }

    @Override
    public void actualizar(Lector lector) {
        String sql = "UPDATE lector SET nombre = ?, correo = ?, tipo = ?, activo = ? WHERE id = ?";
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            ps.setString(1, lector.getNombre());
            ps.setString(2, lector.getCorreo());
            ps.setString(3, lector.getTipo().name());
            ps.setInt(4, lector.isActivo() ? 1 : 0);
            ps.setLong(5, lector.getId());
            if (ps.executeUpdate() == 0) {
                throw new RepositorioException("No existe el lector con id " + lector.getId() + ".", null);
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo actualizar el lector.", e);
        }
    }

    @Override
    public Optional<Lector> buscarPorId(long id) {
        return consultarUno("SELECT " + COLUMNAS + " FROM lector WHERE id = ?", ps -> ps.setLong(1, id));
    }

    @Override
    public Optional<Lector> buscarPorCodigo(String codigoUniversitario) {
        return consultarUno("SELECT " + COLUMNAS + " FROM lector WHERE codigo_universitario = ?",
                ps -> ps.setString(1, codigoUniversitario));
    }

    @Override
    public List<Lector> buscar(String texto) {
        String patron = "%" + (texto == null ? "" : texto.trim().toLowerCase()) + "%";
        String sql = "SELECT " + COLUMNAS + " FROM lector "
                + "WHERE LOWER(codigo_universitario) LIKE ? OR LOWER(nombre) LIKE ? "
                + "OR LOWER(correo) LIKE ? ORDER BY nombre";
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                List<Lector> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo buscar lectores.", e);
        }
    }

    @FunctionalInterface
    private interface Parametros {
        void asignar(PreparedStatement ps) throws SQLException;
    }

    private Optional<Lector> consultarUno(String sql, Parametros parametros) {
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            parametros.asignar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo consultar el lector.", e);
        }
    }

    private static Lector mapear(ResultSet rs) throws SQLException {
        return Lector.reconstruir(
                rs.getLong("id"),
                rs.getString("codigo_universitario"),
                rs.getString("nombre"),
                rs.getString("correo"),
                TipoLector.valueOf(rs.getString("tipo")),
                rs.getInt("activo") == 1);
    }
}
