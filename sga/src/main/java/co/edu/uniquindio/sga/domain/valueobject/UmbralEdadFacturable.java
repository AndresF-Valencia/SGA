package co.edu.uniquindio.sga.domain.valueobject;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**

 * Edad a partir de la cual un ocupante genera cargo (L-09).

 * El valor lo define cada alojamiento: es configuración, no una constante.

 */

public record UmbralEdadFacturable(int anios) {

    public UmbralEdadFacturable {

        if (anios < 0 ) {

            throw new ReglaDominioException(

                "El umbral de edad facturable debe ser como minimo 3 años");

        }

    }

    /**
     * Evalúa si una edad cumple con el umbral configurado para ser facturable.
     */
    public boolean esFacturable(int aniosOcupante) {
        return aniosOcupante >= this.anios;
    }
}

