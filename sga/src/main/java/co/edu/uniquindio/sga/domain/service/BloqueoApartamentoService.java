package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.List;

/**
 * Servicio de Dominio: BloqueoApartamentoService
 * RN-07, 7.3: Garantiza que un apartamento no pueda ser bloqueado administrativamente
 * si ya cuenta con reservas activas en el periodo solicitado.
 */
public class BloqueoApartamentoService {

    /**
     * Valida las reglas de negocio y crea un nuevo Bloqueo administrativo.
     */
    public Bloqueo crearBloqueo(IdentificacionBloqueo idBloqueo,
                                IdentificacionApartamento apartamento,
                                Periodo periodo,
                                MotivoBloqueo motivo,
                                List<Reserva> reservasExistentes) {

        if (idBloqueo == null || apartamento == null || periodo == null || motivo == null) {
            throw new ReglaDominioException("Todos los datos del bloqueo (ID, apartamento, periodo y motivo) son obligatorios");
        }

        // 1. Validar que no existan reservas activas en el periodo a bloquear (7.3)
        validarSinReservasActivas(apartamento, periodo, reservasExistentes);

        // 2. Retornar el nuevo agregado de Bloqueo (nace VIGENTE por construcción)
        return new Bloqueo(idBloqueo, apartamento, periodo, motivo);
    }

    /**
     * Regla 7.3: Si hay reservas PENDIENTES, CONFIRMADAS o EN_CURSO cuyas noches 
     * caigan dentro del periodo del bloqueo, se rechaza la creación.
     */
    private void validarSinReservasActivas(IdentificacionApartamento apartamento, Periodo periodo, List<Reserva> reservasExistentes) {
        if (reservasExistentes == null || reservasExistentes.isEmpty()) {
            return;
        }

        for (Reserva reserva : reservasExistentes) {
            if (reserva.getApartamento().equals(apartamento)) {
                EstadoReserva estado = reserva.getEstado();
                if (estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EN_CURSO) {
                    
                    // Verificamos si alguna de las noches de la estancia de la reserva está incluida en el periodo del bloqueo
                    boolean seSolapan = reserva.getEstancia().nochesOcupadas().stream()
                            .anyMatch(periodo::incluye);

                    if (seSolapan) {
                        throw new ReglaDominioException(
                            String.format("No se puede crear el bloqueo: el apartamento tiene la reserva %s activa en el periodo solicitado",
                                reserva.getCodigo().valor())
                        );
                    }
                }
            }
        }
    }
}