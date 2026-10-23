package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Representa la cantidad de ocupantes de un apartamento.
 * CantidadOcupantes
 * 
 * @param cantidad
 */

public record CapacidadOcupantes(int cantidad) {

    public CapacidadOcupantes {

        if (cantidad < 1) {

            throw new IllegalArgumentException("La cantidad de ocupantes debe ser al menos 1");

        }
    }
}