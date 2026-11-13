package edu.biblioteca.service;

import edu.biblioteca.model.Ejemplar;
import edu.biblioteca.model.estado.TipoEstado;

/**
 * ÚNICO lugar donde las pruebas de préstamos construyen un Ejemplar. Si la forma de crear/
 * reconstruir un Ejemplar en la clase de A es distinta, se ajusta solo aquí.
 */
final class EjemplarDePrueba {
    private EjemplarDePrueba() { }

    static Ejemplar crear(long id, String codigo, TipoEstado estado) {
        return new Ejemplar(id, codigo, 1L, estado);
    }
}
