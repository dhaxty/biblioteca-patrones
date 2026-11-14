package edu.biblioteca.controller;

import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.model.UsuarioSistema;
import edu.biblioteca.security.GestionCuentas;
import edu.biblioteca.security.Rol;
import edu.biblioteca.service.ReglaNegocioException;
import edu.biblioteca.view.PersonalPanel;

import java.util.ArrayList;
import java.util.List;

public class PersonalController {

    private final PersonalPanel vista;
    private final GestionCuentas servicio;
    private final PublicadorEventos eventos;

    public PersonalController(PersonalPanel vista, GestionCuentas servicio, PublicadorEventos eventos) {
        this.vista = vista;
        this.servicio = servicio;
        this.eventos = eventos;

        conectar();
    }

    private void conectar() {
        vista.addCrearListener(e -> crear());
        vista.addDesactivarListener(e -> desactivar());

        eventos.suscribir(tipo -> {
            if (tipo == TipoEvento.CUENTAS_CAMBIO) {
                refrescar();
            }
        });

        refrescar();
    }

    private void crear() {
        try {
            Rol rol = "Administrador".equalsIgnoreCase(vista.getRol()) ? Rol.ADMINISTRADOR : Rol.BIBLIOTECARIO;
            servicio.crearCuenta(vista.getUsuario(), vista.getClave().toCharArray(), rol);
            vista.limpiarFormulario();
            eventos.publicar(TipoEvento.CUENTAS_CAMBIO);
            vista.mostrarMensaje("Cuenta creada correctamente.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al crear cuenta: " + ex.getMessage());
        }
    }

    private void desactivar() {
        String usuario = vista.getUsuarioSeleccionado();
        if (usuario == null) {
            vista.mostrarError("Seleccione primero una cuenta de la tabla.");
            return;
        }
        try {
            servicio.desactivarCuenta(usuario);
            eventos.publicar(TipoEvento.CUENTAS_CAMBIO);
            vista.mostrarMensaje("Cuenta desactivada.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al desactivar cuenta: " + ex.getMessage());
        }
    }

    private void refrescar() {
        try {
            List<UsuarioSistema> cuentas = servicio.listarCuentas();
            List<String[]> filas = new ArrayList<>();
            for (UsuarioSistema u : cuentas) {
                filas.add(new String[]{
                    u.getUsuario(),
                    u.getRol().name(),
                    u.estaActivo() ? "Sí" : "No"
                });
            }
            vista.mostrarCuentas(filas);
        } catch (ReglaNegocioException ex) {
            vista.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError("Error al listar cuentas: " + ex.getMessage());
        }
    }
}
