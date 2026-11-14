package edu.biblioteca.repository.jdbc;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Libro;
import edu.biblioteca.repository.LibroRepository;
import edu.biblioteca.repository.RepositorioException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLibroRepository implements LibroRepository {

    private static final String COLUMNAS = "id, isbn, titulo, autor, editorial, anio, categoria, activo";

    private final Database db;

    public JdbcLibroRepository(Database db) {
        this.db = db;
    }

    @Override
    public Libro guardar(Libro libro) {
        String sql = "INSERT INTO libro (isbn, titulo, autor, editorial, anio, categoria, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            ps.setString(1, libro.getIsbn());
            ps.setString(2, libro.getTitulo());
            ps.setString(3, libro.getAutor());
            ps.setString(4, libro.getEditorial());
            ps.setInt(5, libro.getAnio());
            ps.setString(6, libro.getCategoria());
            ps.setInt(7, libro.isActivo() ? 1 : 0);
            ps.executeUpdate();
            libro.asignarId(db.ultimoIdInsertado());
            return libro;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo guardar el libro.", e);
        }
    }

    @Override
    public Optional<Libro> buscarPorId(long id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM libro WHERE id = ?", id);
    }

    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) {
        return buscarUno("SELECT " + COLUMNAS + " FROM libro WHERE isbn = ?", isbn);
    }

    @Override
    public List<Libro> buscar(String texto, boolean soloActivos) {
        String patron = "%" + (texto == null ? "" : texto.trim().toLowerCase()) + "%";
        String sql = "SELECT " + COLUMNAS + " FROM libro WHERE "
                + "(LOWER(titulo) LIKE ? OR LOWER(autor) LIKE ? OR LOWER(isbn) LIKE ? "
                + " OR id IN (SELECT libro_id FROM ejemplar WHERE LOWER(codigo) LIKE ?)) "
                + (soloActivos ? "AND activo = 1 " : "")
                + "ORDER BY titulo COLLATE NOCASE";
        return consultar(sql, patron, patron, patron, patron);
    }

    @Override
    public void actualizar(Libro libro) {
        String sql = "UPDATE libro SET titulo = ?, autor = ?, editorial = ?, anio = ?, categoria = ?, activo = ? "
                + "WHERE id = ?";
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getEditorial());
            ps.setInt(4, libro.getAnio());
            ps.setString(5, libro.getCategoria());
            ps.setInt(6, libro.isActivo() ? 1 : 0);
            ps.setLong(7, libro.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo actualizar el libro.", e);
        }
    }

    @Override
    public long contarActivos() {
        try (PreparedStatement ps = db.conexion().prepareStatement("SELECT COUNT(*) FROM libro WHERE activo = 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo contar los libros.", e);
        }
    }

    // --- auxiliares ---

    private Optional<Libro> buscarUno(String sql, Object parametro) {
        List<Libro> resultado = consultar(sql, parametro);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    private List<Libro> consultar(String sql, Object... parametros) {
        try (PreparedStatement ps = db.conexion().prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Libro> libros = new ArrayList<>();
                while (rs.next()) {
                    libros.add(mapear(rs));
                }
                return libros;
            }
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo consultar los libros.", e);
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
