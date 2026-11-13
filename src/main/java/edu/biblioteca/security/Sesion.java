package edu.biblioteca.security;

import edu.biblioteca.model.UsuarioSistema;
import java.util.Objects;

public final class Sesion {
    private final UsuarioSistema usuario;
    private boolean activa;

    Sesion(UsuarioSistema usuario) {
        this.usuario = Objects.requireNonNull(usuario, "El usuario no puede ser nulo");
        this.activa = true;
    }

    public UsuarioSistema getUsuario() {
        if (!activa) {
            throw new IllegalStateException("La sesion esta cerrada.");
        }
        return usuario;
    }

    public boolean estaActiva() {
        return activa;
    }

    void cerrar() {
        activa = false;
    }
}
