package edu.biblioteca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.Lector;
import edu.biblioteca.model.RegistroMantenimiento;
import edu.biblioteca.model.TipoLector;
import edu.biblioteca.model.estado.TipoEstado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

class BibliotecaFacadeTest {

    private Database db;
    private Fakes.Ejemplares ejemplares;
    private BibliotecaFacade facade;
    private final List<String> eventos = new ArrayList<>();
    private long idIngresoDanio = -1;
    private String motivoIngresoDanio;

    @BeforeEach
    void preparar() {
        db = Database.enMemoria();
        Fakes.Lectores lectores = new Fakes.Lectores();
        ejemplares = new Fakes.Ejemplares();
        Fakes.Prestamos prestamos = new Fakes.Prestamos();
        lectores.guardar(new Lector("U001", "Ana", "ana@uni.edu", TipoLector.ESTUDIANTE));
        ejemplares.agregar(7, "EJ-1", TipoEstado.DISPONIBLE);

        Clock reloj = Clock.fixed(LocalDate.of(2026, 11, 2).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        PrestamoService prestamoService = new PrestamoService(db, lectores, ejemplares, prestamos, reloj);

        // Solo interesa saber si la Facade llama a registrarIngresoPorDanio y con qué datos.
        MantenimientoService mantenimiento = new MantenimientoService(null, null, null) {
            @Override
            public RegistroMantenimiento registrarIngresoPorDanio(long ejemplarId, String motivo) {
                idIngresoDanio = ejemplarId;
                motivoIngresoDanio = motivo;
                return null;
            }
        };
        facade = new BibliotecaFacade(db, prestamoService, mantenimiento, eventos::add);
    }

    @AfterEach
    void cerrar() {
        db.close();
    }

    @Test
    void prestarPublicaElEventoSoloSiTodoSaleBien() {
        assertThrows(ReglaNegocioException.class, () -> facade.prestar("NO-EXISTE", "EJ-1", 1));
        assertEquals(List.of(), eventos);

        facade.prestar("U001", "EJ-1", 1);
        assertEquals(List.of(BibliotecaFacade.EVENTO_PRESTAMO_REGISTRADO), eventos);
    }

    @Test
    void devolucionNormalNoTocaMantenimiento() {
        facade.prestar("U001", "EJ-1", 1);
        eventos.clear();

        facade.devolver("EJ-1", false, null);

        assertEquals(TipoEstado.DISPONIBLE, ejemplares.porCodigo.get("EJ-1").getTipoEstado());
        assertEquals(-1L, idIngresoDanio);
        assertEquals(List.of(BibliotecaFacade.EVENTO_DEVOLUCION_REGISTRADA), eventos);
    }

    @Test
    void devolucionConDanioDejaEnMantenimientoYRegistraElIngreso() {
        facade.prestar("U001", "EJ-1", 1);
        eventos.clear();

        facade.devolver("EJ-1", true, "  Páginas arrancadas ");

        assertEquals(TipoEstado.EN_MANTENIMIENTO, ejemplares.porCodigo.get("EJ-1").getTipoEstado());
        assertEquals(7L, idIngresoDanio);
        assertEquals("Páginas arrancadas", motivoIngresoDanio);
        assertEquals(List.of(BibliotecaFacade.EVENTO_DEVOLUCION_REGISTRADA,
                BibliotecaFacade.EVENTO_MANTENIMIENTO_ACTUALIZADO), eventos);
    }

    @Test
    void devolucionConDanioSinMotivoSeRechazaAntesDeTocarNada() {
        facade.prestar("U001", "EJ-1", 1);
        eventos.clear();

        assertThrows(ReglaNegocioException.class, () -> facade.devolver("EJ-1", true, "  "));

        assertEquals(TipoEstado.PRESTADO, ejemplares.porCodigo.get("EJ-1").getTipoEstado());
        assertEquals(List.of(), eventos);
    }
}
