package co.edu.uniquindio.sga.domain.valueobject;

/**
 * Resultado inmutable de evaluar la cancelación de una reserva.
 */
public record ResultadoCancelacion(
        TramoCancelacion tramoAplicado,
        Dinero penalidad,
        Dinero reembolso) {
}