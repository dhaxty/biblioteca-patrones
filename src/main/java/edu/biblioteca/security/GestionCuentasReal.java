package edu.biblioteca.security;

import edu.biblioteca.model.UsuarioSistema;
import edu.biblioteca.repository.UsuarioSistemaRepository;
import edu.biblioteca.service.ReglaNegocioException;

import java.util.Arrays;
import java.util.Objects;

final class GestionCuentasReal implements GestionCuentas {
    private final UsuarioSistemaRepository repositorio;

    GestionCuentasReal(UsuarioSistemaRepository repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    @Override
    public void crearCuenta(String usuario, char[] contrasena, Rol rol) {
        try {
            if (usuario == null || usuario.isBlank()) {
                throw new ReglaNegocioException("El usuario es obligatorio.");
            }
            if (contrasena == null || contrasena.length == 0) {
                throw new ReglaNegocioException("La contraseña es obligatoria.");
            }
            if (rol == null) {
                throw new ReglaNegocioException("El rol es obligatorio.");
            }

            String usuarioLimpio = usuario.trim();

            if (repositorio.buscarPorUsuario(usuarioLimpio).isPresent()) {
                throw new ReglaNegocioException("Ese usuario ya existe.");
            }

            String hash = HashContrasena.crearHash(contrasena);
            UsuarioSistema nuevaCuenta =
                    new UsuarioSistema(usuarioLimpio, hash, rol, true);
            repositorio.crear(nuevaCuenta);
        } finally {
            if (contrasena != null) {
                Arrays.fill(contrasena, '\0');
            }
        }
    }

    @Override
    public void desactivarCuenta(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            throw new ReglaNegocioException("El usuario es obligatorio.");
        }
        repositorio.desactivar(usuario.trim());
    }
}
