package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDateTime;
import java.util.List;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Política de cancelación inmutable que contiene una lista de tramos de anticipación.
 */
public record PoliticaCancelacion(
        String nombre,
        List<TramoCancelacion> tramos) {

    public PoliticaCancelacion {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("La política de cancelación debe tener un nombre");
        }
        if (tramos == null || tramos.isEmpty()) {
            throw new ReglaDominioException("La política de cancelación debe contener al menos un tramo");
        }
        //copia inmutable de la lista
        tramos = List.copyOf(tramos);
    }

    /**
     * Evalúa el momento de la cancelación respecto a la estancia y calcula la penalidad/reembolso.
     */
    public ResultadoCancelacion evaluar(LocalDateTime momentoCancelacion, Estancia estancia, Dinero valorTotal) {
        if (momentoCancelacion == null) {
            throw new ReglaDominioException("Se requiere el momento de la cancelación para evaluar la política");
        }
        if (estancia == null) {
            throw new ReglaDominioException("Se requiere la estancia de la reserva para evaluar la política");
        }
        if (valorTotal == null || valorTotal.esNegativo()) {
            throw new ReglaDominioException("El valor total a evaluar no puede ser nulo ni negativo");
        }

        // 1. Encontrar el tramo que aplica según la anticipación de la estancia
        TramoCancelacion tramoAplicable = tramos.stream()
                .filter(tramo -> tramo.aplicaPara(momentoCancelacion, estancia))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException(
                        "No se encontró un tramo de cancelación aplicable para los días de anticipación de la estancia"));

        // 2. Calcular valores monetarios
        // Si el tramo devuelve 100%, la penalidad es 0%. Si devuelve 70%, la penalidad es 30%.
        double porcentajePenalidad = 100.0 - tramoAplicable.porcentajeDevolucion();
        
        Dinero penalidad = valorTotal.porcentual(porcentajePenalidad);
        Dinero reembolso = valorTotal.menos(penalidad);

        return new ResultadoCancelacion(tramoAplicable, penalidad, reembolso);
    }
}