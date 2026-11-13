package edu.biblioteca.model;

import edu.biblioteca.security.Rol;
import java.util.Objects;

public class UsuarioSistema {
    private final String usuario;
    private final String hashContrasena;
    private Rol rol;
    private boolean activo;

    public UsuarioSistema(String usuario, String hashContrasena, Rol rol, boolean activo) {
        this.usuario = Objects.requireNonNull(usuario, "El usuario no puede ser nulo");
        this.hashContrasena = Objects.requireNonNull(hashContrasena, "El hash de contrasena no puede ser nulo");
        this.rol = Objects.requireNonNull(rol, "El rol no puede ser nulo");
        this.activo = activo;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean tieneRol(Rol rolRequerido) {
        return this.rol == rolRequerido;
    }

    public boolean estaActivo() {
        return activo;
    }

    public boolean isActivo() {
        return estaActivo();
    }

    public void setRol(Rol rol) {
        this.rol = Objects.requireNonNull(rol, "El rol no puede ser nulo");
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
