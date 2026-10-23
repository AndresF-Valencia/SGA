package co.edu.uniquindio.sga.domain.valueobject;

public enum CanalOrigen {
    PORTAL,
    DIRECTO,
    EXTERNO;

     /**

     * RN-19: toda reserva externa llega con un identificador propio del canal,

     * y la combinación canal + identificador es única.

     */

    public boolean exigeIdentificadorExterno() {

        return this == EXTERNO;

    }

}
