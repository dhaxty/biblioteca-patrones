package edu.biblioteca.repository.jdbc;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.estado.TipoEstado;
import edu.biblioteca.repository.EjemplarRepository;
import edu.biblioteca.repository.RepositorioException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEjemplarRepository implements EjemplarRepository {

    private static final String COLUMNAS = "id, codigo, libro_id, estado";

    private final Database db;

    public JdbcEjemplarRepository(Database db) {
        this.db = db;
    }

    @Override
    public Ejemplar guardar(Ejemplar ejemplar) {
        String sql = "INSERT INTO ejemplar (codigo, libro_id, estado) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            ps.setString(1, ejemplar.getCodigo());
            ps.setLong(2, ejemplar.getLibroId());
            ps.setString(3, ejemplar.getTipoEstado().name());
            ps.executeUpdate();
            ejemplar.asignarId(db.ultimoIdInsertado());
            return ejemplar;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo guardar el ejemplar.", e);
        }
    }

    @Override
    public Optional<Ejemplar> buscarPorId(long id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM ejemplar WHERE id = ?", id);
    }

    @Override
    public Optional<Ejemplar> buscarPorCodigo(String codigo) {
        return buscarUno("SELECT " + COLUMNAS + " FROM ejemplar WHERE codigo = ?", codigo);
    }

    @Override
    public List<Ejemplar> listarPorLibro(long libroId) {
        return consultar("SELECT " + COLUMNAS + " FROM ejemplar WHERE libro_id = ? ORDER BY codigo", libroId);
    }

    @Override
    public List<Ejemplar> listarPorEstado(TipoEstado estado) {
        return consultar("SELECT " + COLUMNAS + " FROM ejemplar WHERE estado = ? ORDER BY codigo", estado.name());
    }

    @Override
    public void actualizar(Ejemplar ejemplar) {
        try (PreparedStatement ps = db.conexion().prepareStatement("UPDATE ejemplar SET estado = ? WHERE id = ?")) {
            ps.setString(1, ejemplar.getTipoEstado().name());
            ps.setLong(2, ejemplar.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo actualizar el ejemplar.", e);
        }
    }

    @Override
    public long contarPorEstado(TipoEstado estado) {
        try (PreparedStatement ps = db.conexion().prepareStatement("SELECT COUNT(*) FROM ejemplar WHERE estado = ?")) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo contar los ejemplares.", e);
        }
    }

    // --- auxiliares ---

    private Optional<Ejemplar> buscarUno(String sql, Object parametro) {
        List<Ejemplar> resultado = consultar(sql, parametro);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    private List<Ejemplar> consultar(String sql, Object... parametros) {
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Ejemplar> ejemplares = new ArrayList<>();
                while (rs.next()) {
                    ejemplares.add(new Ejemplar(
                            rs.getLong("id"),
                            rs.getString("codigo"),
                            rs.getLong("libro_id"),
                            TipoEstado.valueOf(rs.getString("estado"))));
                }
                return ejemplares;
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo consultar los ejemplares.", e);
        }
    }
}
