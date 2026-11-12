package biblioteca;

import biblioteca.config.ConexionSqlite;
import biblioteca.model.Ejemplar;
import biblioteca.model.Libro;
import biblioteca.model.RegistroMantenimiento;
import biblioteca.repository.RepositoryException;
import biblioteca.repository.jdbc.EjemplarRepositoryJdbc;
import biblioteca.repository.jdbc.LibroRepositoryJdbc;
import biblioteca.repository.jdbc.MantenimientoRepositoryJdbc;
import biblioteca.service.CatalogoService;
import biblioteca.service.MantenimientoService;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

/**
 * Consola de PRUEBA del Módulo A (no es la UI final; esa es del Módulo D).
 *
 * Uso:
 *   Main              menú interactivo, base "biblioteca.db"
 *   Main --demo       recorrido automático sobre una base en memoria
 *   Main archivo.db   menú interactivo sobre otro archivo
 */
public class Main {

    private final CatalogoService catalogo;
    private final MantenimientoService mantenimiento;
    private final EjemplarRepositoryJdbc ejemplares;

    private Main(Connection connection) {
        LibroRepositoryJdbc libros = new LibroRepositoryJdbc(connection);
        this.ejemplares = new EjemplarRepositoryJdbc(connection);
        MantenimientoRepositoryJdbc registros = new MantenimientoRepositoryJdbc(connection);
        this.catalogo = new CatalogoService(libros, ejemplares);
        this.mantenimiento = new MantenimientoService(connection, ejemplares, registros);
    }

    public static void main(String[] args) throws Exception {
        boolean demo = args.length > 0 && args[0].equals("--demo");
        try (Connection connection = demo
                ? ConexionSqlite.abrirEnMemoria()
                : ConexionSqlite.abrirArchivo(args.length > 0 ? args[0] : "biblioteca.db")) {
            ConexionSqlite.inicializarEsquema(connection);
            Main app = new Main(connection);
            if (demo) {
                app.demo();
            } else {
                app.menu();
            }
        }
    }

    // ------------------------------------------------------------------ demo

    private void demo() {
        System.out.println("=== DEMO AUTOMÁTICA ===");
        Libro libro = catalogo.registrarLibro("978-0-306-40615-7", "Cien años de soledad",
                "Gabriel García Márquez", "Sudamericana", 1967, "Novela");
        System.out.println("1. Libro registrado: " + libro);

        Ejemplar ej = catalogo.registrarEjemplar("EJ-001", libro.getId());
        System.out.println("2. Ejemplar registrado: " + ej);

        ej.prestar();
        ejemplares.actualizar(ej);
        System.out.println("3. Prestado: " + estadoActual(ej.getId()));

        intentar("4. Enviar a mantenimiento estando PRESTADO (debe fallar)",
                () -> mantenimiento.enviarAMantenimiento(ej.getId(), "Tapa rota"));

        ej.devolver();
        ejemplares.actualizar(ej);
        System.out.println("5. Devuelto: " + estadoActual(ej.getId()));

        RegistroMantenimiento reg = mantenimiento.enviarAMantenimiento(ej.getId(), "Tapa rota");
        System.out.println("6. En mantenimiento: " + estadoActual(ej.getId()) + " | ingreso " + reg.getIngreso());

        intentar("7. Habilitar dos veces (la segunda debe fallar)", () -> {
            mantenimiento.habilitar(ej.getId());
            mantenimiento.habilitar(ej.getId());
        });

        System.out.println("8. Estado final: " + estadoActual(ej.getId()));
        imprimirHistorial(ej.getId());

        catalogo.desactivarLibro(libro.getId());
        System.out.println("9. Libro desactivado. Activos: " + catalogo.listarLibrosActivos().size()
                + ", total: " + catalogo.listarLibros().size());
        System.out.println("=== FIN DE LA DEMO ===");
    }

    private void intentar(String descripcion, Runnable accion) {
        try {
            accion.run();
            System.out.println(descripcion + " -> OK");
        } catch (IllegalArgumentException | IllegalStateException | RepositoryException e) {
            System.out.println(descripcion + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private String estadoActual(long ejemplarId) {
        return catalogo.obtenerEjemplar(ejemplarId).getEstadoNombre();
    }

    // ------------------------------------------------------------------ menú

    private void menu() {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("""

                    ===== BIBLIOTECA - Módulo A =====
                     1) Registrar libro          7) Devolver ejemplar
                     2) Listar libros            8) Enviar a mantenimiento
                     3) Actualizar libro         9) Habilitar ejemplar
                     4) Desactivar libro        10) Ver historial de mantenimiento
                     5) Registrar ejemplar      11) Listar todos los ejemplares
                     6) Prestar ejemplar         0) Salir
                    """);
            String opcion = leer(in, "Opción");
            if (opcion == null || opcion.equals("0")) {
                System.out.println("Hasta luego.");
                return;
            }
            try {
                ejecutar(opcion, in);
            } catch (NumberFormatException e) { // subclase de IllegalArgumentException: va primero
                System.out.println("ERROR: se esperaba un número.");
            } catch (IllegalArgumentException | IllegalStateException | RepositoryException e) {
                System.out.println("ERROR (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            }
        }
    }

    private void ejecutar(String opcion, Scanner in) {
        switch (opcion) {
            case "1" -> {
                Libro l = catalogo.registrarLibro(
                        leer(in, "ISBN"), leer(in, "Título"), leer(in, "Autor"),
                        leer(in, "Editorial"), Integer.parseInt(leer(in, "Año")), leer(in, "Categoría"));
                System.out.println("Registrado: " + l);
            }
            case "2" -> listarLibros();
            case "3" -> {
                Libro l = catalogo.actualizarLibro(
                        Long.parseLong(leer(in, "Id del libro")), leer(in, "Título"), leer(in, "Autor"),
                        leer(in, "Editorial"), Integer.parseInt(leer(in, "Año")), leer(in, "Categoría"));
                System.out.println("Actualizado: " + l);
            }
            case "4" -> {
                catalogo.desactivarLibro(Long.parseLong(leer(in, "Id del libro")));
                System.out.println("Libro desactivado (borrado lógico).");
            }
            case "5" -> {
                Ejemplar e = catalogo.registrarEjemplar(leer(in, "Código único"),
                        Long.parseLong(leer(in, "Id del libro")));
                System.out.println("Registrado: " + e);
            }
            case "6" -> {
                Ejemplar e = catalogo.obtenerEjemplar(Long.parseLong(leer(in, "Id del ejemplar")));
                e.prestar();
                ejemplares.actualizar(e);
                System.out.println("Ahora: " + e);
            }
            case "7" -> {
                Ejemplar e = catalogo.obtenerEjemplar(Long.parseLong(leer(in, "Id del ejemplar")));
                e.devolver();
                ejemplares.actualizar(e);
                System.out.println("Ahora: " + e);
            }
            case "8" -> {
                RegistroMantenimiento r = mantenimiento.enviarAMantenimiento(
                        Long.parseLong(leer(in, "Id del ejemplar")), leer(in, "Motivo"));
                System.out.println("Ingresó a mantenimiento el " + r.getIngreso());
            }
            case "9" -> {
                RegistroMantenimiento r = mantenimiento.habilitar(Long.parseLong(leer(in, "Id del ejemplar")));
                System.out.println("Habilitado el " + r.getHabilitacion());
            }
            case "10" -> imprimirHistorial(Long.parseLong(leer(in, "Id del ejemplar")));
            case "11" -> listarEjemplares();
            default -> System.out.println("Opción no válida.");
        }
    }

    private void listarLibros() {
        List<Libro> libros = catalogo.listarLibros();
        if (libros.isEmpty()) {
            System.out.println("(sin libros)");
        }
        for (Libro l : libros) {
            System.out.printf("[%d] %s | %s | %s (%d) | %s%n", l.getId(), l.getIsbn(), l.getTitulo(),
                    l.getAutor(), l.getAnio(), l.isActivo() ? "ACTIVO" : "INACTIVO");
        }
    }

    private void listarEjemplares() {
        List<Ejemplar> lista = ejemplares.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("(sin ejemplares)");
        }
        for (Ejemplar e : lista) {
            System.out.printf("[%d] %s | %s | %s%n", e.getId(), e.getCodigoUnico(),
                    e.getLibro().getTitulo(), e.getEstadoNombre());
        }
    }

    private void imprimirHistorial(long ejemplarId) {
        List<RegistroMantenimiento> historial = mantenimiento.historial(ejemplarId);
        System.out.println("Historial del ejemplar " + ejemplarId + ":");
        if (historial.isEmpty()) {
            System.out.println("  (sin registros)");
        }
        for (RegistroMantenimiento r : historial) {
            System.out.printf("  #%d | %s | ingreso %s | habilitación %s%n", r.getId(), r.getMotivo(),
                    r.getIngreso(), r.getHabilitacion() == null ? "(abierto)" : r.getHabilitacion());
        }
    }

    private static String leer(Scanner in, String etiqueta) {
        System.out.print(etiqueta + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }
}
