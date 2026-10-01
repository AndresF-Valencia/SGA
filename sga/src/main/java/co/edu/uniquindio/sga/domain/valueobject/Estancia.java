package co.edu.uniquindio.sga.domain.valueobject;
import java.time.LocalDate;

import java.time.temporal.ChronoUnit;

import java.util.List;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**

 * Rango continuo de noches en que un apartamento queda ocupado.

 * Intervalo cerrado en la entrada y abierto en la salida: [entrada, salida).

 */

public record Estancia(LocalDate fechaEntrada, LocalDate fechaSalida) {

    public Estancia {

        if (fechaEntrada == null || fechaSalida == null) {

            throw new ReglaDominioException("La estancia requiere fecha de entrada y de salida");

        }

        // RN-03: toda estancia tiene al menos una noche

        if (!fechaSalida.isAfter(fechaEntrada)) {

            throw new ReglaDominioException(

                "La fecha de salida debe ser posterior a la fecha de entrada");

        }

    }

    /** Del 10 al 12 son dos noches: la del 10 y la del 11. */

    public int noches() {

        return (int) ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);

    }

    /** La noche de la fecha de salida no se ocupa ni se cobra. */

    public boolean incluye(LocalDate noche) {

        return !noche.isBefore(fechaEntrada) && noche.isBefore(fechaSalida);

    }

    /** RN-01: dos estancias se solapan si comparten al menos una noche. */

    public boolean seSolapaCon(Estancia otra) {

        return this.fechaEntrada.isBefore(otra.fechaSalida)

            && otra.fechaEntrada.isBefore(this.fechaSalida);

    }

    /** Las noches efectivamente ocupadas, útiles para liquidar noche por noche (RN-05). */

    public List<LocalDate> nochesOcupadas() {

        return fechaEntrada.datesUntil(fechaSalida).toList();

    }

}

