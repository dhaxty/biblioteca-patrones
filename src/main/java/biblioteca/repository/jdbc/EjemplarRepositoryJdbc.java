package biblioteca.repository.jdbc;

import biblioteca.model.Ejemplar;
import biblioteca.model.Libro;
import biblioteca.model.estado.EstadoEjemplar;
import biblioteca.repository.EjemplarRepository;
import biblioteca.repository.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EjemplarRepositoryJdbc implements EjemplarRepository {

    private static final String SELECT_BASE =
            "SELECT e.id AS ej_id, e.codigo_unico, e.estado, "
                    + "l.id AS lb_id, l.isbn, l.titulo, l.autor, l.editorial, l.anio, l.categoria, l.activo "
                    + "FROM ejemplar e JOIN libro l ON l.id = e.libro_id ";

    private final Connection connection;

    public EjemplarRepositoryJdbc(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula");
        }
        this.connection = connection;
    }

    @Override
    public Ejemplar guardar(Ejemplar ejemplar) {
        if (ejemplar.getLibro().getId() == null) {
            throw new IllegalArgumentException("El libro del ejemplar debe estar guardado primero");
        }
        String sql = "INSERT INTO ejemplar (codigo_unico, libro_id, estado) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ejemplar.getCodigoUnico());
            ps.setLong(2, ejemplar.getLibro().getId());
            ps.setString(3, ejemplar.getEstadoNombre());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    ejemplar.setId(keys.getLong(1));
                }
            }
            return ejemplar;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo guardar el ejemplar " + ejemplar.getCodigoUnico(), e);
        }
    }

    @Override
    public void actualizar(Ejemplar ejemplar) {
        if (ejemplar.getId() == null) {
            throw new IllegalArgumentException("El ejemplar no tiene id; guárdalo primero");
        }
        String sql = "UPDATE ejemplar SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ejemplar.getEstadoNombre());
            ps.setLong(2, ejemplar.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe el ejemplar con id " + ejemplar.getId());
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo actualizar el ejemplar " + ejemplar.getId(), e);
        }
    }

    @Override
    public Optional<Ejemplar> buscarPorId(long id) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BASE + "WHERE e.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo buscar el ejemplar " + id, e);
        }
    }

    @Override
    public Optional<Ejemplar> buscarPorCodigo(String codigoUnico) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BASE + "WHERE e.codigo_unico = ?")) {
            ps.setString(1, codigoUnico);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo buscar el ejemplar " + codigoUnico, e);
        }
    }

    @Override
    public List<Ejemplar> buscarPorLibro(long libroId) {
        try (PreparedStatement ps = connection.prepareStatement(
                SELECT_BASE + "WHERE e.libro_id = ? ORDER BY e.codigo_unico")) {
            ps.setLong(1, libroId);
            return leerLista(ps);
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo listar los ejemplares del libro " + libroId, e);
        }
    }

    @Override
    public List<Ejemplar> listarTodos() {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BASE + "ORDER BY e.codigo_unico")) {
            return leerLista(ps);
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo listar los ejemplares", e);
        }
    }

    private List<Ejemplar> leerLista(PreparedStatement ps) throws SQLException {
        List<Ejemplar> resultado = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        }
        return resultado;
    }

    private static Ejemplar mapear(ResultSet rs) throws SQLException {
        Libro libro = new Libro(
                rs.getLong("lb_id"),
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("editorial"),
                rs.getInt("anio"),
                rs.getString("categoria"),
                rs.getInt("activo") == 1);
        return new Ejemplar(
                rs.getLong("ej_id"),
                rs.getString("codigo_unico"),
                libro,
                EstadoEjemplar.desdeNombre(rs.getString("estado")));
    }
}
