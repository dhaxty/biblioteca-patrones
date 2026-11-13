package edu.biblioteca.event;

public class ObservadorDashboard implements ObservadorEvento {

    /** Acción que refresca el panel (la entrega el DashboardController). */
    private final Runnable actualizarPanel;

    public ObservadorDashboard(Runnable actualizarPanel) {
        this.actualizarPanel = actualizarPanel;
    }

    @Override
    public void alRecibirEvento(TipoEvento tipo) {
        // Los cambios de cuentas del personal no afectan a los indicadores.
        if (tipo != TipoEvento.CUENTAS_CAMBIO) {
            actualizarPanel.run();
        }
    }
}
