package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Servicio de Dominio: RegistroLlegadaService (check-in)
 * RN-08, RN-10, RN-11: el grupo toma posesión del apartamento.
 *
 * Coordina dos agregados que cambian juntos:
 * - la Reserva pasa de CONFIRMADA a EN_CURSO (ella valida RN-08 y RN-10);
 * - el Apartamento pasa de PREPARADO a OCUPADO.
 *
 * Primero se revisa que el apartamento pueda recibir al grupo (RN-11). Si se
 * hiciera al revés, la reserva podría quedar EN_CURSO con un apartamento que
 * no está listo.
 */
public class RegistroLlegadaService {

    public void registrarLlegada(Reserva reserva, Apartamento apartamento, String autor, LocalDateTime ahora) {
        if (reserva == null || apartamento == null || ahora == null) {
            throw new ReglaDominioException("La reserva, el apartamento y la marca temporal son obligatorios");
        }
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("Debe indicarse quién registra la llegada");
        }
        if (!reserva.getApartamento().equals(apartamento.getIdentificacion())) {
            throw new ReglaDominioException("La reserva no corresponde a este apartamento");
        }

        // RN-11: solo se entrega un apartamento activo y PREPARADO
        if (!apartamento.puedeRecibirGrupo()) {
            throw new ReglaDominioException(
                    "El apartamento no puede recibir al grupo: su estado operativo es "
                            + apartamento.getEstadoOperativo());
        }

        // La reserva valida que esté CONFIRMADA y que ya sea la fecha de entrada
        reserva.registrarLlegada(autor, ahora);

        // PREPARADO → OCUPADO; no puede fallar porque ya se verificó arriba
        apartamento.marcarOcupado();
    }
}
