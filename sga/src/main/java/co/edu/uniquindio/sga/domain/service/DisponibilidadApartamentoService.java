package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

import java.util.List;

/**
 * Servicio de Dominio: DisponibilidadApartamentoService
 * RN-01, RN-02, RN-07, RN-12, RN-20 y límite de mascotas: valida si un
 * apartamento puede recibir una estancia.
 *
 * Se usa al crear una reserva y al modificarla (RN-14); en la modificación se
 * pasa el código de la propia reserva para que no choque consigo misma.
 */
public class DisponibilidadApartamentoService {

    /**
     * Verifica la disponibilidad completa del apartamento.
     * Lanza una ReglaDominioException con el motivo si falla alguna regla.
     *
     * @param reservaExcluida   código de la reserva que se está modificando, o null
     * @param alojamiento       da las horas de entrada y salida para RN-20
     */
    public void verificarDisponibilidad(Apartamento apartamento, Estancia estancia, int totalOcupantes,
                                        CantidadMascota mascotas, CodigoReserva reservaExcluida,
                                        List<Reserva> reservasExistentes, List<Bloqueo> bloqueosExistentes,
                                        Alojamiento alojamiento) {
        if (apartamento == null || estancia == null || alojamiento == null) {
            throw new ReglaDominioException(
                    "El apartamento, la estancia y el alojamiento son obligatorios para verificar disponibilidad");
        }

        // 1. El apartamento debe estar activo y su capacidad debe alcanzar (RN-02)
        if (!apartamento.estaActivo()) {
            throw new ReglaDominioException("El apartamento seleccionado no se encuentra activo");
        }
        if (!apartamento.admite(totalOcupantes)) {
            throw new ReglaDominioException("La capacidad del apartamento es insuficiente para el grupo de ocupantes");
        }

        // 2. Límite de mascotas del apartamento
        int cantidadMascotas = mascotas == null ? 0 : mascotas.cantidad();
        if (!apartamento.admiteMascotas(cantidadMascotas)) {
            throw new ReglaDominioException(String.format(
                    "El apartamento admite como máximo %d mascotas y la solicitud trae %d",
                    apartamento.getLimiteMascotas().limite(), cantidadMascotas));
        }

        // 3. Solapamiento y tiempo de preparación con reservas activas (RN-01, RN-12, RN-20)
        if (reservasExistentes != null) {
            for (Reserva reserva : reservasExistentes) {
                if (!reservaCuenta(reserva, apartamento, reservaExcluida)) {
                    continue;
                }
                if (estancia.seSolapaCon(reserva.getEstancia())) {
                    throw new ReglaDominioException(
                            "El apartamento ya se encuentra reservado en el rango de fechas solicitado");
                }
                verificarTiempoPreparacion(estancia, reserva.getEstancia(),
                        apartamento.getTiempoPreparacion(), alojamiento);
            }
        }

        // 4. Bloqueos vigentes (RN-07)
        if (bloqueosExistentes != null) {
            for (Bloqueo bloqueo : bloqueosExistentes) {
                if (bloqueo.getApartamento().equals(apartamento.getIdentificacion()) && bloqueo.afecta(estancia)) {
                    throw new ReglaDominioException(
                            "El apartamento se encuentra bloqueado administrativamente en el rango solicitado");
                }
            }
        }
    }

    /** Consulta rápida: true si está disponible, false si viola alguna regla. */
    public boolean estaDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes,
                                  CantidadMascota mascotas, List<Reserva> reservasExistentes,
                                  List<Bloqueo> bloqueosExistentes, Alojamiento alojamiento) {
        try {
            verificarDisponibilidad(apartamento, estancia, totalOcupantes, mascotas, null,
                    reservasExistentes, bloqueosExistentes, alojamiento);
            return true;
        } catch (ReglaDominioException e) {
            return false;
        }
    }

    /**
     * Solo cuentan las reservas activas (PENDIENTE, CONFIRMADA, EN_CURSO) del
     * mismo apartamento, sin contar la que se está modificando.
     */
    private boolean reservaCuenta(Reserva reserva, Apartamento apartamento, CodigoReserva reservaExcluida) {
        if (reservaExcluida != null && reserva.getCodigo().equals(reservaExcluida)) {
            return false;
        }
        return reserva.getApartamento().equals(apartamento.getIdentificacion())
                && reserva.getEstado().retieneDisponibilidad();
    }

    /**
     * RN-20: cuando un grupo sale el mismo día en que entra el siguiente, entre
     * la hora de salida y la de entrada debe caber el tiempo de preparación.
     */
    private void verificarTiempoPreparacion(Estancia nueva, Estancia existente,
                                            TiempoPreparacion tiempo, Alojamiento alojamiento) {
        boolean mismoDia = nueva.fechaEntrada().equals(existente.fechaSalida())
                || existente.fechaEntrada().equals(nueva.fechaSalida());
        if (mismoDia && !tiempo.alcanzaEntre(alojamiento.getHoraSalida(), alojamiento.getHoraEntrada())) {
            throw new ReglaDominioException(String.format(
                    "No hay tiempo de preparación suficiente (%d horas) entre la salida de las %s y la entrada de las %s",
                    tiempo.tiempoPreparacion(), alojamiento.getHoraSalida(), alojamiento.getHoraEntrada()));
        }
    }
}
