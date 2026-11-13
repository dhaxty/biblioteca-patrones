package edu.biblioteca.security;

import edu.biblioteca.model.UsuarioSistema;
import edu.biblioteca.repository.UsuarioSistemaRepository;
import edu.biblioteca.service.ReglaNegocioException;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class AutenticacionService {
    private final UsuarioSistemaRepository repositorio;

    public AutenticacionService(UsuarioSistemaRepository repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio no puede ser nulo");
    }

    public Optional<Sesion> iniciarSesion(String usuario, char[] contrasena) {
        try {
            if (usuario == null || usuario.isBlank() || contrasena == null) {
                return Optional.empty();
            }

            Optional<UsuarioSistema> cuenta =
                    repositorio.buscarPorUsuario(usuario.trim());

            return cuenta
                    .filter(UsuarioSistema::estaActivo)
                    .filter(c -> HashContrasena.verificar(
                            contrasena, c.getHashContrasena()))
                    .map(Sesion::new);
        } finally {
            if (contrasena != null) {
                Arrays.fill(contrasena, '\0');
            }
        }
    }

    public void crearAdministradorInicial(String usuario, char[] contrasena) {
        try {
            if (usuario == null || usuario.isBlank()) {
                throw new ReglaNegocioException("El usuario es obligatorio.");
            }
            if (contrasena == null || contrasena.length == 0) {
                throw new ReglaNegocioException("La contraseña es obligatoria.");
            }
            if (repositorio.existeAlgunaCuenta()) {
                throw new ReglaNegocioException(
                        "La cuenta inicial solo se crea cuando no hay cuentas.");
            }

            String hash = HashContrasena.crearHash(contrasena);
            UsuarioSistema administrador = new UsuarioSistema(
                    usuario.trim(),
                    hash,
                    Rol.ADMINISTRADOR,
                    true
            );

            repositorio.crear(administrador);
        } finally {
            if (contrasena != null) {
                Arrays.fill(contrasena, '\0');
            }
        }
    }

    public void cerrarSesion(Sesion sesion) {
        if (sesion != null && sesion.estaActiva()) {
            sesion.cerrar();
        }
    }
}
