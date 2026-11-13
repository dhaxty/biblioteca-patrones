package edu.biblioteca.event;

/**
 * Patrón OBSERVER - rol "Observer" (interfaz del observador).
 * Cualquier clase que quiera enterarse de los cambios implementa este contrato.
 */
@FunctionalInterface
public interface ObservadorEvento {

    /**
     * Se invoca cuando el publicador anuncia un evento.
     *
     * @param tipo qué tipo de cambio ocurrió
     */
    void alRecibirEvento(TipoEvento tipo);
}
