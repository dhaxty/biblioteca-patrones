package biblioteca;

import biblioteca.config.ConexionSqlite;
import biblioteca.model.Libro;

import java.sql.Connection;
import java.sql.SQLException;

/** Utilidades de prueba: base SQLite en memoria con el esquema real. */
public final class BaseDatosDePrueba {

    private BaseDatosDePrueba() {
    }

    public static Connection nueva() throws SQLException {
        Connection connection = ConexionSqlite.abrirEnMemoria();
        ConexionSqlite.inicializarEsquema(connection);
        return connection;
    }

    public static Libro libroEjemplo(String isbn) {
        return new Libro(isbn, "Cien años de soledad", "Gabriel García Márquez",
                "Sudamericana", 1967, "Novela");
    }
}
