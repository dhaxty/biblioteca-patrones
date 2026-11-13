package edu.biblioteca.security;

import edu.biblioteca.repository.UsuarioSistemaRepository;
import java.util.Objects;

public final class GestionCuentasFactory {
    private GestionCuentasFactory() {
    }

    public static GestionCuentas crear(
            UsuarioSistemaRepository repositorio, Sesion sesion) {
        Objects.requireNonNull(repositorio, "El repositorio no puede ser nulo");
        Objects.requireNonNull(sesion, "La sesion no puede ser nula");

        GestionCuentas servicioReal =
                new GestionCuentasReal(repositorio);

        return new GestionCuentasProxy(servicioReal, sesion);
    }
}
