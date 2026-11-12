package biblioteca.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Apertura de conexiones SQLite e inicialización del esquema. */
public final class ConexionSqlite {

    private static final String RECURSO_ESQUEMA = "/sql/schema.sql";

    private ConexionSqlite() {
    }

    /** Abre una conexión con las llaves foráneas activadas. */
    public static Connection abrir(String url) throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }

    public static Connection abrirArchivo(String ruta) throws SQLException {
        return abrir("jdbc:sqlite:" + ruta);
    }

    /** Base de datos en memoria; vive mientras la conexión siga abierta (útil para pruebas). */
    public static Connection abrirEnMemoria() throws SQLException {
        return abrir("jdbc:sqlite::memory:");
    }

    /** Ejecuta {@code schema.sql} sobre la conexión (idempotente: usa IF NOT EXISTS). */
    public static void inicializarEsquema(Connection connection) throws SQLException {
        String script = leerEsquema();
        try (Statement st = connection.createStatement()) {
            for (String sentencia : script.split(";")) {
                String limpia = sentencia.trim();
                if (!limpia.isEmpty()) {
                    st.execute(limpia);
                }
            }
        }
    }

    private static String leerEsquema() {
        try (InputStream in = ConexionSqlite.class.getResourceAsStream(RECURSO_ESQUEMA)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso " + RECURSO_ESQUEMA);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + RECURSO_ESQUEMA, e);
        }
    }
}
