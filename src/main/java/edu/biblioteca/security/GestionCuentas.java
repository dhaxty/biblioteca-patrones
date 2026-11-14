package edu.biblioteca.security;

import edu.biblioteca.model.UsuarioSistema;
import java.util.List;

public interface GestionCuentas {
    void crearCuenta(String usuario, char[] contrasena, Rol rol);

    void desactivarCuenta(String usuario);

    List<UsuarioSistema> listarCuentas();
}
