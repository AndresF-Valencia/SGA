package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** Normas de convivencia del alojamiento (7.3). Puede estar vacía, pero nunca es nula. */
public record NormasConvivencia(String texto) {

    public static final int MAXIMO_CARACTERES = 2000;
    public static final NormasConvivencia VACIAS = new NormasConvivencia("");

    public NormasConvivencia {
        texto = texto == null ? "" : texto.strip();
        if (texto.length() > MAXIMO_CARACTERES) {
            throw new ReglaDominioException(
                    "Las normas de convivencia no pueden superar los " + MAXIMO_CARACTERES + " caracteres");
        }
    }
}
