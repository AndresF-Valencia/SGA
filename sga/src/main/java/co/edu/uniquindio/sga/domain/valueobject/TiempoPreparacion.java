package co.edu.uniquindio.sga.domain.valueobject;

/**
 * representa el tiempo de preparación de un apartamento
 * 
 * @param tiempoPreparacion el tiempo de preparación del apartamento
 * por defecto es de 3 horas
 */

public record TiempoPreparacion(int tiempoPreparacion) {

    public TiempoPreparacion {
        tiempoPreparacion = 3;
    }

}
