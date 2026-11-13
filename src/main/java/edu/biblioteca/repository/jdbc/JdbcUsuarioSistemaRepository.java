package edu.biblioteca.repository.jdbc;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.UsuarioSistema;
import edu.biblioteca.repository.RepositorioException;
import edu.biblioteca.repository.UsuarioSistemaRepository;
import edu.biblioteca.security.Rol;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class JdbcUsuarioSistemaRepository implements UsuarioSistemaRepository {
    private final Database db;

    public JdbcUsuarioSistemaRepository(Database db) {
        this.db = Objects.requireNonNull(db, "Database no puede ser nulo");
    }

    @Override
    public Optional<UsuarioSistema> buscarPorUsuario(String usuario) {
        String sql = """
                SELECT usuario, hash_contrasena, rol, activo
                FROM usuario_sistema
                WHERE usuario = ?
                """;

        try (PreparedStatement consulta = db.conexion().prepareStatement(sql)) {
            consulta.setString(1, usuario);

            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }

                UsuarioSistema cuenta = new UsuarioSistema(
                        resultado.getString("usuario"),
                        resultado.getString("hash_contrasena"),
                        Rol.valueOf(resultado.getString("rol")),
                        resultado.getInt("activo") == 1
                );

                return Optional.of(cuenta);
            }
        } catch (SQLException | IllegalArgumentException ex) {
            throw new RepositorioException("No se pudo buscar la cuenta.", ex);
        }
    }

    @Override
    public boolean existeAlgunaCuenta() {
        String sql = "SELECT 1 FROM usuario_sistema LIMIT 1";

        try (PreparedStatement consulta = db.conexion().prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            return resultado.next();
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo comprobar si existen cuentas.", ex);
        }
    }

    @Override
    public void crear(UsuarioSistema usuario) {
        String sql = """
                INSERT INTO usuario_sistema
                    (usuario, hash_contrasena, rol, activo)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement insercion = db.conexion().prepareStatement(sql)) {
            insercion.setString(1, usuario.getUsuario());
            insercion.setString(2, usuario.getHashContrasena());
            insercion.setString(3, usuario.getRol().name());
            insercion.setInt(4, usuario.isActivo() ? 1 : 0);
            insercion.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo crear la cuenta.", ex);
        }
    }

    @Override
    public void desactivar(String usuario) {
        String sql = """
                UPDATE usuario_sistema
                SET activo = 0
                WHERE usuario = ?
                """;

        try (PreparedStatement actualizacion = db.conexion().prepareStatement(sql)) {
            actualizacion.setString(1, usuario);
            int filasActualizadas = actualizacion.executeUpdate();

            if (filasActualizadas == 0) {
                throw new RepositorioException("No existe una cuenta con ese usuario.", null);
            }
        } catch (SQLException ex) {
            throw new RepositorioException("No se pudo desactivar la cuenta.", ex);
        }
    }
}
