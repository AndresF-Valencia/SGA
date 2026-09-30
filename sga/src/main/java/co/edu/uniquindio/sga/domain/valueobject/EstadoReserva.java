package co.edu.uniquindio.sga.domain.valueobject;

public enum EstadoReserva {
    PENDIENTE(true),
    CONFIRMADA(true),
    EN_CURSO(true),
    FINALIZADA(false),
    CANCELADA(false),
    NO_SHOW(false);

    private final boolean activa;

    EstadoReserva(boolean activa) {

        this.activa = activa;

    }

    /** Reservas activas: PENDIENTE, CONFIRMADA y EN_CURSO. */

    public boolean esActiva() {

        return activa;

    }

    /** FINALIZADA, CANCELADA y NO_SHOW son estados terminales. */

    public boolean esTerminal() {

        return !activa;

    }

    /**

     * En este dominio solo las reservas activas retienen noches (RN-12).

     * Se expone como método aparte porque el negocio habla de las dos cosas

     * y podría dejar de coincidir en otro alojamiento.

     */

    public boolean retieneDisponibilidad() {

        return activa;

    }


}
