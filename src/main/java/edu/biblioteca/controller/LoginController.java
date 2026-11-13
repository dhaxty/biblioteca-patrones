package edu.biblioteca.controller;

import edu.biblioteca.security.AutenticacionService;
import edu.biblioteca.security.Sesion;
import edu.biblioteca.service.ReglaNegocioException;
import edu.biblioteca.view.LoginFrame;

import java.util.Optional;
import java.util.function.Consumer;

public class LoginController {

    private final LoginFrame vista;
    private final AutenticacionService servicio;
    private final Consumer<Sesion> alIngresar;

    public LoginController(LoginFrame vista, AutenticacionService servicio, Consumer<Sesion> alIngresar) {
        this.vista = vista;
        this.servicio = servicio;
        this.alIngresar = alIngresar;
        this.vista.addIngresarListener(e -> intentarIngreso());
    }

    public LoginController(LoginFrame vista, AutenticacionService servicio, Runnable alIngresar) {
        this(vista, servicio, s -> alIngresar.run());
    }

    private void intentarIngreso() {
        String usuario = vista.getUsuario();
        String clave = vista.getClave();

        if (usuario.isEmpty() || clave.isEmpty()) {
            vista.mostrarMensaje("Ingrese su usuario y contraseña.");
            return;
        }

        try {
            Optional<Sesion> sesionOpt = servicio.iniciarSesion(usuario, clave.toCharArray());
            if (sesionOpt.isPresent()) {
                vista.mostrarMensaje("");
                alIngresar.accept(sesionOpt.get());
            } else {
                vista.mostrarMensaje("Usuario o contraseña incorrectos.");
            }
        } catch (ReglaNegocioException ex) {
            vista.mostrarMensaje(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarMensaje("Error al iniciar sesión: " + ex.getMessage());
        }
    }
}
