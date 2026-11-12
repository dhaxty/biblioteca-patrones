package biblioteca.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibroTest {

    private Libro libro() {
        return new Libro("978-0-306-40615-7", " Título ", "Autor", "Editorial", 2000, "Ciencia");
    }

    @Test
    void creaLibroActivoYNormalizaDatos() {
        Libro l = libro();
        assertTrue(l.isActivo());
        assertEquals("9780306406157", l.getIsbn());
        assertEquals("Título", l.getTitulo());
    }

    @Test
    void aceptaIsbn10ConDigitoX() {
        assertEquals("080442957X", new Libro("0-8044-2957-X", "T", "A", "E", 1990, "C").getIsbn());
    }

    @Test
    void rechazaIsbnInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Libro(null, "T", "A", "E", 2000, "C"));
        assertThrows(IllegalArgumentException.class, () -> new Libro(" ", "T", "A", "E", 2000, "C"));
        assertThrows(IllegalArgumentException.class, () -> new Libro("12345", "T", "A", "E", 2000, "C"));
        assertThrows(IllegalArgumentException.class, () -> new Libro("ABCDEFGHIJKLM", "T", "A", "E", 2000, "C"));
    }

    @Test
    void rechazaCamposVaciosOAnioInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Libro("9780306406157", "", "A", "E", 2000, "C"));
        assertThrows(IllegalArgumentException.class, () -> new Libro("9780306406157", "T", null, "E", 2000, "C"));
        assertThrows(IllegalArgumentException.class, () -> new Libro("9780306406157", "T", "A", " ", 2000, "C"));
        assertThrows(IllegalArgumentException.class, () -> new Libro("9780306406157", "T", "A", "E", 0, "C"));
        assertThrows(IllegalArgumentException.class, () -> new Libro("9780306406157", "T", "A", "E", 2000, ""));
    }

    @Test
    void actualizarCambiaDatosPeroNoElIsbn() {
        Libro l = libro();
        l.actualizar("Nuevo", "Otro", "Ed2", 2010, "Arte");
        assertEquals("Nuevo", l.getTitulo());
        assertEquals(2010, l.getAnio());
        assertEquals("9780306406157", l.getIsbn());
    }

    @Test
    void actualizarInvalidoNoModificaElLibro() {
        Libro l = libro();
        assertThrows(IllegalArgumentException.class, () -> l.actualizar("Nuevo", "", "Ed2", 2010, "Arte"));
        assertEquals("Título", l.getTitulo());
    }

    @Test
    void desactivarMarcaInactivo() {
        Libro l = libro();
        l.desactivar();
        assertFalse(l.isActivo());
    }
}
