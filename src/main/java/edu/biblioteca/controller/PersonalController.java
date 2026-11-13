package edu.biblioteca.controller;

import edu.biblioteca.config.Database;
import edu.biblioteca.event.PublicadorEventos;
import edu.biblioteca.event.TipoEvento;
import edu.biblioteca.security.GestionCuentas;
import edu.biblioteca.security.Rol;
import edu.biblioteca.service.ReglaNegocioException;
import edu.biblioteca.view.PersonalPanel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class PersonalController {

    private final PersonalPanel vista;
    private final GestionCuentas servicio;
    private final Database db;
    private final Supplier<List<String[]>> proveedorCuentas;
    private final PublicadorEventos eventos;

    public PersonalController(PersonalPanel vista, GestionCuentas servicio,
                              Database db, PublicadorEventos eventos) {
        this.vista = vista;
        this.servicio = servicio;
        this.db = db;
        this.proveedorCuentas = null;
        this.eventos = eventos;

        conectar();
    }

    public PersonalController(PersonalPanel vista, GestionCuentas servicio,
                              Supplier<List<String[]>> proveedorCuentas, PublicadorEventos eventos) {
        this.vista = vista;
        this.servicio = servicio;
        this.db = null;
        this.proveedorCuentas = proveedorCuentas;
        this.eventos = eventos;

        conectar();
    }

    public PersonalController(PersonalPanel vista, GestionCuentas servicio, PublicadorEventos eventos) {
        this(vista, servicio, (Database) null, eventos);
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
            if (proveedorCuentas != null) {
                vista.mostrarCuentas(proveedorCuentas.get());
                return;
            }
            if (db != null) {
                List<String[]> filas = new ArrayList<>();
                String sql = "SELECT usuario, rol, activo FROM usuario_sistema ORDER BY usuario";
                try (PreparedStatement ps = db.conexion().prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        filas.add(new String[]{
                            rs.getString("usuario"),
                            rs.getString("rol"),
                            rs.getInt("activo") == 1 ? "Sí" : "No"
                        });
                    }
                }
                vista.mostrarCuentas(filas);
                return;
            }
            vista.mostrarCuentas(Collections.emptyList());
        } catch (SQLException ex) {
            vista.mostrarError("Error al listar cuentas: " + ex.getMessage());
        } catch (Exception ex) {
            vista.mostrarError(ex.getMessage());
        }
    }
}
