package edu.biblioteca.config;

import edu.biblioteca.repository.RepositorioException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

/**
 * Acceso a SQLite con una única conexión compartida (aplicación de escritorio, un solo proceso).
 * Los repositorios usan {@link #conexion()}; el Service/Facade agrupa operaciones con {@link #enTransaccion}.
 * Las transacciones son reentrantes: si ya hay una abierta, el bloque interno participa en ella.
 */
public final class Database implements AutoCloseable {

    private final String url;
    private Connection conexion;
    private int profundidad = 0;

    public Database(String rutaArchivo) {
        this.url = "jdbc:sqlite:" + rutaArchivo;
    }

    /** Base temporal para pruebas. */
    public static Database enMemoria() {
        return new Database(":memory:");
    }

    public synchronized Connection conexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(url);
                try (Statement st = conexion.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
            }
            return conexion;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo abrir la base de datos.", e);
        }
    }

    /** Ejecuta los scripts SQL indicados (rutas dentro de src/main/resources), p. ej. "/db/schema-catalogo.sql". */
    public void inicializarEsquema(String... recursos) {
        for (String recurso : recursos) {
            String script = leerRecurso(recurso);
            try (Statement st = conexion().createStatement()) {
                for (String sentencia : script.split(";")) {
                    if (!sentencia.isBlank()) {
                        st.execute(sentencia);
                    }
                }
            } catch (SQLException e) {
                throw new RepositorioException("Error al ejecutar el script " + recurso, e);
            }
        }
    }

    /** Id de la última fila insertada en esta conexión (funciona con cualquier versión del driver). */
    public long ultimoIdInsertado() {
        try (Statement st = conexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RepositorioException("No se pudo obtener el id insertado.", e);
        }
    }

    public <T> T enTransaccion(Supplier<T> trabajo) {
        synchronized (this) {
            if (profundidad > 0) {
                return trabajo.get(); // la transacción externa decide commit/rollback
            }
            Connection c = conexion();
            try {
                c.setAutoCommit(false);
                profundidad = 1;
                T resultado = trabajo.get();
                c.commit();
                return resultado;
            } catch (RuntimeException e) {
                revertir(c);
                throw e;
            } catch (SQLException e) {
                revertir(c);
                throw new RepositorioException("Error al confirmar la transacción.", e);
            } finally {
                profundidad = 0;
                restaurarAutoCommit(c);
            }
        }
    }

    public void enTransaccion(Runnable trabajo) {
        enTransaccion(() -> {
            trabajo.run();
            return null;
        });
    }

    private static void revertir(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignorada) {
            // si el rollback falla no hay nada más que hacer; se propaga el error original
        }
    }

    private static void restaurarAutoCommit(Connection c) {
        try {
            c.setAutoCommit(true);
        } catch (SQLException ignorada) {
            // idem
        }
    }

    private String leerRecurso(String ruta) {
        try (InputStream in = Database.class.getResourceAsStream(ruta)) {
            if (in == null) {
                throw new RepositorioException("No se encontró el recurso " + ruta, null);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositorioException("No se pudo leer el recurso " + ruta, e);
        }
    }

    @Override
    public synchronized void close() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error al cerrar la base de datos.", e);
        }
    }
}
