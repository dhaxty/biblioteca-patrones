package edu.biblioteca.security;

public interface GestionCuentas {
    void crearCuenta(String usuario, char[] contrasena, Rol rol);

    void desactivarCuenta(String usuario);
}
