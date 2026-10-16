package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

import java.util.List;

/**
 * Servicio de Dominio: DisponibilidadApartamentoService
 * RN-01, RN-02, RN-07, RN-12, RN-20: Valida si un apartamento está libre de reservas
 * activas, bloqueos y cumple con la capacidad y tiempo de preparación.
 */
public class DisponibilidadApartamentoService {

    /**
     * Verifica la disponibilidad completa del apartamento para una estancia y cantidad de ocupantes.
     * Lanza una ReglaDominioException si choca con alguna regla de negocio.
     */
    public void verificarDisponibilidad(Apartamento apartamento, Estancia estancia, int totalOcupantes,
                                      CodigoReserva reservaExcluida, List<Reserva> reservasExistentes,
                                      List<Bloqueo> bloqueosExistentes, TiempoPreparacion tiempoPreparacion) {
        if (apartamento == null || estancia == null) {
            throw new ReglaDominioException("El apartamento y la estancia son obligatorios para verificar disponibilidad");
        }

        // 1. Validar que el apartamento esté activo y su capacidad alcance (RN-02)
        if (!apartamento.estaActivo()) {
            throw new ReglaDominioException("El apartamento seleccionado no se encuentra activo");
        }
        if (!apartamento.admite(totalOcupantes)) {
            throw new ReglaDominioException("La capacidad del apartamento es insuficiente para el grupo de ocupantes");
        }

        // 2. Verificar solapamiento con reservas activas (RN-01, RN-12)
        // Se consideran activas: PENDIENTE, CONFIRMADA, EN_CURSO
        if (reservasExistentes != null) {
            for (Reserva reserva : reservasExistentes) {
                // Si estamos modificando una reserva, excluimos su propio código para que no choque consigo misma
                if (reservaExcluida != null && reserva.getCodigo().equals(reservaExcluida)) {
                    continue; 
                }
                
                if (reserva.getApartamento().equals(apartamento.getIdentificacion())) {
                    if (reserva.getEstado() == EstadoReserva.PENDIENTE ||
                        reserva.getEstado() == EstadoReserva.CONFIRMADA ||
                        reserva.getEstado() == EstadoReserva.EN_CURSO) {
                        
                        if (estancia.seSolapaCon(reserva.getEstancia())) {
                            throw new ReglaDominioException("El apartamento ya se encuentra reservado en el rango de fechas solicitado");
                        }
                    }
                }
            }
        }

        // 3. Verificar solapamiento con bloqueos vigentes (RN-07)
        // Aprovecha directamente el método afecta(Estancia) encapsulado en la entidad Bloqueo
        if (bloqueosExistentes != null) {
            for (Bloqueo bloqueo : bloqueosExistentes) {
                if (bloqueo.getApartamento().equals(apartamento.getIdentificacion())) {
                    if (bloqueo.afecta(estancia)) {
                        throw new ReglaDominioException("El apartamento se encuentra bloqueado administrativamente en el rango solicitado");
                    }
                }
            }
        }
    }

    /**
     * Método auxiliar de consulta rápida: retorna true si está disponible, false si viola alguna regla.
     */
    public boolean estaDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes,
                                 List<Reserva> reservasExistentes, List<Bloqueo> bloqueosExistentes,
                                 TiempoPreparacion tiempoPreparacion) {
        try {
            verificarDisponibilidad(apartamento, estancia, totalOcupantes, null, reservasExistentes, bloqueosExistentes, tiempoPreparacion);
            return true;
        } catch (ReglaDominioException e) {
            return false;
        }
    }
}