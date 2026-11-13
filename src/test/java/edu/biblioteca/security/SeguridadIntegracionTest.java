package edu.biblioteca.security;

import edu.biblioteca.config.Database;
import edu.biblioteca.model.UsuarioSistema;
import edu.biblioteca.repository.UsuarioSistemaRepository;
import edu.biblioteca.repository.jdbc.JdbcUsuarioSistemaRepository;
import edu.biblioteca.service.ReglaNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeguridadIntegracionTest {

    private Database db;
    private UsuarioSistemaRepository repositorio;
    private AutenticacionService autenticacion;

    @BeforeEach
    void preparar() {
        db = Database.enMemoria();
        db.inicializarEsquema("/db/schema-seguridad.sql");
        repositorio = new JdbcUsuarioSistemaRepository(db);
        autenticacion = new AutenticacionService(repositorio);
    }

    @AfterEach
    void cerrar() {
        db.close();
    }

    @Test
    void creaAdministradorInicialYIniciaSesion() {
        assertFalse(repositorio.existeAlgunaCuenta());

        autenticacion.crearAdministradorInicial("admin", "Admin123*".toCharArray());

        assertTrue(repositorio.existeAlgunaCuenta());
        Optional<UsuarioSistema> cuenta = repositorio.buscarPorUsuario("admin");
        assertTrue(cuenta.isPresent());
        assertTrue(cuenta.get().tieneRol(Rol.ADMINISTRADOR));
        assertTrue(cuenta.get().estaActivo());
        assertNotNull(cuenta.get().getHashContrasena());

        // Login exitoso
        Optional<Sesion> sesion = autenticacion.iniciarSesion("admin", "Admin123*".toCharArray());
        assertTrue(sesion.isPresent());
        assertTrue(sesion.get().estaActiva());
        assertEquals("admin", sesion.get().getUsuario().getUsuario());

        // Login con credenciales incorrectas
        Optional<Sesion> fallida = autenticacion.iniciarSesion("admin", "ClaveErronea".toCharArray());
        assertFalse(fallida.isPresent());
    }

    @Test
    void rechazaSegundoAdministradorInicial() {
        autenticacion.crearAdministradorInicial("admin", "Admin123*".toCharArray());

        assertThrows(ReglaNegocioException.class,
                () -> autenticacion.crearAdministradorInicial("admin2", "OtraClave".toCharArray()));
    }

    @Test
    void proxyPermiteCrearCuentasSoloAAdministrador() {
        autenticacion.crearAdministradorInicial("admin", "Admin123*".toCharArray());
        Sesion sesionAdmin = autenticacion.iniciarSesion("admin", "Admin123*".toCharArray()).orElseThrow();

        GestionCuentas gestionAdmin = GestionCuentasFactory.crear(repositorio, sesionAdmin);
        gestionAdmin.crearCuenta("bibliotecario1", "Biblio123*".toCharArray(), Rol.BIBLIOTECARIO);

        // Validar que la cuenta de bibliotecario fue creada
        Optional<UsuarioSistema> biblio = repositorio.buscarPorUsuario("bibliotecario1");
        assertTrue(biblio.isPresent());
        assertTrue(biblio.get().tieneRol(Rol.BIBLIOTECARIO));

        // Iniciar sesión como bibliotecario
        Sesion sesionBiblio = autenticacion.iniciarSesion("bibliotecario1", "Biblio123*".toCharArray()).orElseThrow();
        GestionCuentas gestionBiblio = GestionCuentasFactory.crear(repositorio, sesionBiblio);

        // El bibliotecario NO debe poder crear cuentas (debe lanzar ReglaNegocioException)
        assertThrows(ReglaNegocioException.class,
                () -> gestionBiblio.crearCuenta("otro", "Clave123*".toCharArray(), Rol.BIBLIOTECARIO));
    }

    @Test
    void proxyBloqueaSiSesionEstaCerrada() {
        autenticacion.crearAdministradorInicial("admin", "Admin123*".toCharArray());
        Sesion sesion = autenticacion.iniciarSesion("admin", "Admin123*".toCharArray()).orElseThrow();
        GestionCuentas gestion = GestionCuentasFactory.crear(repositorio, sesion);

        autenticacion.cerrarSesion(sesion);
        assertFalse(sesion.estaActiva());

        assertThrows(ReglaNegocioException.class,
                () -> gestion.crearCuenta("nuevo", "Clave123*".toCharArray(), Rol.BIBLIOTECARIO));
    }

    @Test
    void desactivaCuentaCorrectamente() {
        autenticacion.crearAdministradorInicial("admin", "Admin123*".toCharArray());
        Sesion sesionAdmin = autenticacion.iniciarSesion("admin", "Admin123*".toCharArray()).orElseThrow();
        GestionCuentas gestion = GestionCuentasFactory.crear(repositorio, sesionAdmin);

        gestion.crearCuenta("operador", "Operador123*".toCharArray(), Rol.BIBLIOTECARIO);
        gestion.desactivarCuenta("operador");

        UsuarioSistema cuenta = repositorio.buscarPorUsuario("operador").orElseThrow();
        assertFalse(cuenta.estaActivo());

        // Usuario inactivo no puede iniciar sesión
        Optional<Sesion> loginInactivo = autenticacion.iniciarSesion("operador", "Operador123*".toCharArray());
        assertFalse(loginInactivo.isPresent());
    }
}
