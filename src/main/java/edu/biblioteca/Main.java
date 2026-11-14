package edu.biblioteca;

import edu.biblioteca.config.ContextoAplicacion;
import edu.biblioteca.view.CoordinadorVistas;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal de la aplicación Biblioteca Universitaria.
 */
public class Main {

    public static void main(String[] args) {
        CoordinadorVistas.configurarApariencia();
        ContextoAplicacion contexto = ContextoAplicacion.inicializarProduccion();
        CoordinadorVistas coordinador = new CoordinadorVistas(contexto);
        SwingUtilities.invokeLater(coordinador::iniciar);
    }
}
