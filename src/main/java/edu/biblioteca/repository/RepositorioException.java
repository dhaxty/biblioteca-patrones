package edu.biblioteca.repository;

/** Envuelve los errores de acceso a datos (SQLException) para que las capas superiores no dependan de JDBC. */
public class RepositorioException extends RuntimeException {

    public RepositorioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
