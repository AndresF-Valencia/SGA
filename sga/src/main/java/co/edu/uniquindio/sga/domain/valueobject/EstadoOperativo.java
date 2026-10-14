package co.edu.uniquindio.sga.domain.valueobject;

public enum EstadoOperativo {
    PREPARADO,
    OCUPADO,
    PENDIENTE_PREPARACION,
    EN_PREPARACION,
    FUERA_DE_SERVICIO;

    /** RN-11: solo un apartamento PREPARADO puede recibir un grupo. */

    public boolean permiteRegistro() {

        return this == PREPARADO;

    }

    /**
     * Transiciones permitidas del estado operativo (7.6). OCUPADO nunca pasa a
     * FUERA_DE_SERVICIO.
     */
    /**
     * Transiciones permitidas del estado operativo (7.6). OCUPADO nunca pasa a
     * FUERA_DE_SERVICIO.
     */
    public boolean puedeTransicionarA(EstadoOperativo siguiente) {
        return switch (this) {
            case PREPARADO -> siguiente == OCUPADO
                    || siguiente == FUERA_DE_SERVICIO;
            case OCUPADO -> siguiente == PENDIENTE_PREPARACION;
            case PENDIENTE_PREPARACION -> siguiente == EN_PREPARACION
                    || siguiente == FUERA_DE_SERVICIO;
            case EN_PREPARACION -> siguiente == PREPARADO
                    || siguiente == FUERA_DE_SERVICIO;
            case FUERA_DE_SERVICIO -> siguiente == PENDIENTE_PREPARACION;
        };
    }

}
