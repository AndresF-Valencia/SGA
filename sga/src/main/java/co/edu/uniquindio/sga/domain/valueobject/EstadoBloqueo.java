package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Ciclo de vida de un bloqueo. La eliminación es lógica: un bloqueo no se
 * borra, se levanta (12.4).
 */
public enum EstadoBloqueo {
    VIGENTE, LEVANTADO;

    /** RN-07: solo un bloqueo vigente impide vender las noches que cubre. */
    public boolean impideVenta() {
        return this == VIGENTE;
    }
}
