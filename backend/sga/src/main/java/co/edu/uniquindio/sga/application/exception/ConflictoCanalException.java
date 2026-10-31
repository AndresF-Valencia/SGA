package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * RN-18: la reserva externa choca con una reserva o un bloqueo vigente. Se rechaza (409)
 * y el conflicto queda registrado con el id que trae este error.
 */
public class ConflictoCanalException extends ReglaDominioException {

    private final String conflictoId;

    public ConflictoCanalException(String conflictoId, String motivo) {
        super("Conflicto de canal " + conflictoId + ": la reserva externa se rechazó y quedó registrada "
                + "para revisión del administrador. Motivo: " + motivo);
        this.conflictoId = conflictoId;
    }

    public String getConflictoId() {
        return conflictoId;
    }
}
