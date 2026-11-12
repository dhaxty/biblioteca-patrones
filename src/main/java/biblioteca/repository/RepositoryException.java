package biblioteca.repository;

/** Error de infraestructura (SQL/JDBC) envuelto como excepción no verificada. */
public class RepositoryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RepositoryException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
