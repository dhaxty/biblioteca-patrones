package biblioteca.repository.jdbc;

import biblioteca.model.Libro;
import biblioteca.repository.LibroRepository;
import biblioteca.repository.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibroRepositoryJdbc implements LibroRepository {

    private static final String COLUMNAS = "id, isbn, titulo, autor, editorial, anio, categoria, activo";

    private final Connection connection;

    public LibroRepositoryJdbc(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula");
        }
        this.connection = connection;
    }

    @Override
    public Libro guardar(Libro libro) {
        String sql = "INSERT INTO libro (isbn, titulo, autor, editorial, anio, categoria, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, libro.getIsbn());
            ps.setString(2, libro.getTitulo());
            ps.setString(3, libro.getAutor());
            ps.setString(4, libro.getEditorial());
            ps.setInt(5, libro.getAnio());
            ps.setString(6, libro.getCategoria());
            ps.setInt(7, libro.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    libro.setId(keys.getLong(1));
                }
            }
            return libro;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo guardar el libro " + libro.getIsbn(), e);
        }
    }

    @Override
    public void actualizar(Libro libro) {
        if (libro.getId() == null) {
            throw new IllegalArgumentException("El libro no tiene id; guárdalo primero");
        }
        String sql = "UPDATE libro SET titulo = ?, autor = ?, editorial = ?, anio = ?, "
                + "categoria = ?, activo = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getEditorial());
            ps.setInt(4, libro.getAnio());
            ps.setString(5, libro.getCategoria());
            ps.setInt(6, libro.isActivo() ? 1 : 0);
            ps.setLong(7, libro.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe el libro con id " + libro.getId());
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo actualizar el libro " + libro.getId(), e);
        }
    }

    @Override
    public void desactivar(long id) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE libro SET activo = 0 WHERE id = ?")) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe el libro con id " + id);
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo desactivar el libro " + id, e);
        }
    }

    @Override
    public Optional<Libro> buscarPorId(long id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT " + COLUMNAS + " FROM libro WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo buscar el libro " + id, e);
        }
    }

    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT " + COLUMNAS + " FROM libro WHERE isbn = ?")) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo buscar el libro con ISBN " + isbn, e);
        }
    }

    @Override
    public List<Libro> listarTodos() {
        return listar("SELECT " + COLUMNAS + " FROM libro ORDER BY titulo");
    }

    @Override
    public List<Libro> listarActivos() {
        return listar("SELECT " + COLUMNAS + " FROM libro WHERE activo = 1 ORDER BY titulo");
    }

    private List<Libro> listar(String sql) {
        List<Libro> resultado = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo listar los libros", e);
        }
    }

    private static Libro mapear(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getLong("id"),
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("editorial"),
                rs.getInt("anio"),
                rs.getString("categoria"),
                rs.getInt("activo") == 1);
    }
}
