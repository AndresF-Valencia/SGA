package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Representa la cantidad de dormitorios de un apartamento.
 * Dormitorio
 * 
 * @param cantidad
 */

public record Dormitorio(int cantidad) {

    public Dormitorio {

        if (cantidad < 1) {

            throw new IllegalArgumentException("La cantidad de dormitorios debe ser al menos 1");

        }

    }

}