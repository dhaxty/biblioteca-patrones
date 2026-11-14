package edu.biblioteca.security;

import edu.biblioteca.model.UsuarioSistema;
import edu.biblioteca.service.ReglaNegocioException;
import java.util.List;
import java.util.Objects;

public class GestionCuentasProxy implements GestionCuentas {
    private final GestionCuentas servicioReal;
    private final Sesion sesion;

    public GestionCuentasProxy(GestionCuentas servicioReal, Sesion sesion) {
        this.servicioReal = Objects.requireNonNull(servicioReal, "El servicio real no puede ser nulo");
        this.sesion = sesion;
    }

    @Override
    public void crearCuenta(String usuario, char[] contrasena, Rol rol) {
        verificarPermiso();
        servicioReal.crearCuenta(usuario, contrasena, rol);
    }

    @Override
    public void desactivarCuenta(String usuario) {
        verificarPermiso();
        servicioReal.desactivarCuenta(usuario);
    }

    @Override
    public List<UsuarioSistema> listarCuentas() {
        verificarPermiso();
        return servicioReal.listarCuentas();
    }

    private void verificarPermiso() {
        if (sesion == null || !sesion.estaActiva()) {
            throw new ReglaNegocioException("Debes iniciar sesión.");
        }

        if (sesion.getUsuario().getRol() != Rol.ADMINISTRADOR) {
            throw new ReglaNegocioException("Solo un administrador puede gestionar cuentas.");
        }
    }
}
