package co.edu.uniquindio.sga.domain.valueobject;
/**

 * Estados del ciclo de vida de una reserva

 */

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

    /** RN-12: en este dominio solo las reservas activas retienen noches. */
    public boolean retieneDisponibilidad() {

        return activa;

    }

    /**
     * RN-08: define el ciclo de vida válido de una reserva.
     */

    public boolean puedeTransicionarA(EstadoReserva siguiente) {

        return switch (this) {

            case PENDIENTE  -> siguiente == CONFIRMADA || siguiente == CANCELADA;

            case CONFIRMADA -> siguiente == EN_CURSO

                            || siguiente == CANCELADA

                            || siguiente == NO_SHOW;

            case EN_CURSO   -> siguiente == FINALIZADA;

            case FINALIZADA, CANCELADA, NO_SHOW -> false;   // estados terminales

        };

    }

}
