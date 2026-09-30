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

}
