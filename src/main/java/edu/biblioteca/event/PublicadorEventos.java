package edu.biblioteca.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Patrón OBSERVER - rol "Subject" (el que publica).
 * Guarda la lista de observadores suscritos y los avisa cuando se publica un evento.
 * Se usa SOLO después de que la operación se guardó con éxito.
 */
public class PublicadorEventos {

    /** Lista segura: permite suscribirse mientras se está notificando. */
    private final List<ObservadorEvento> observadores = new CopyOnWriteArrayList<>();

    /** Agrega un observador a la lista. */
    public void suscribir(ObservadorEvento observador) {
        observadores.add(observador);
    }

    /** Quita un observador de la lista. */
    public void desuscribir(ObservadorEvento observador) {
        observadores.remove(observador);
    }

    /** Avisa a todos los observadores que ocurrió un cambio. */
    public void publicar(TipoEvento tipo) {
        for (ObservadorEvento observador : observadores) {
            observador.alRecibirEvento(tipo);
        }
    }
}
