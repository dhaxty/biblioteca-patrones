package edu.biblioteca.repository;

import edu.biblioteca.model.UsuarioSistema;
import java.util.Optional;

public interface UsuarioSistemaRepository {
    Optional<UsuarioSistema> buscarPorUsuario(String usuario);
    void crear(UsuarioSistema usuario);
    void desactivar(String usuario);
    boolean existeAlgunaCuenta();
}
