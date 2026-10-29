package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.port.NotificadorReserva;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.ConfirmacionReservaService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;

import java.time.LocalDateTime;

/**
 * Caso de uso (Guía 06): confirmar una reserva exigiendo el anticipo (RNP-03).
 */
 
public class ConfirmarReservaUseCase {

        private final ReservaRepository reservaRepository;
        private final FolioRepository folioRepository;
        private final AlojamientoRepository alojamientoRepository;
        private final ConfirmacionReservaService confirmacionService;
        private final NotificadorReserva notificador;

        public ConfirmarReservaUseCase(ReservaRepository reservaRepository, FolioRepository folioRepository,
                        AlojamientoRepository alojamientoRepository,
                        ConfirmacionReservaService confirmacionService, NotificadorReserva notificador) {
                this.reservaRepository = reservaRepository;
                this.folioRepository = folioRepository;
                this.alojamientoRepository = alojamientoRepository;
                this.confirmacionService = confirmacionService;
                this.notificador = notificador;
        }

        /**
         * Devuelve la reserva para que la API responda con su detalle actualizado (Guía
         * 07).
         */
        @Transactional
        public Reserva ejecutar(CodigoReserva codigo, IdentificacionAlojamiento alojamientoId,
                        String autor, LocalDateTime ahora) {
                Reserva reserva = reservaRepository.buscarPorCodigo(codigo)
                                .orElseThrow(() -> new ReservaNoEncontradaException(codigo));
                Folio folio = folioRepository.buscarPorReserva(codigo)
                                .orElseThrow(() -> new FolioNoEncontradoException(codigo));
                Alojamiento alojamiento = alojamientoRepository.buscarPorId(alojamientoId)
                                .orElseThrow(() -> new AlojamientoNoEncontradoException(alojamientoId));

                // RNP-03: exige el anticipo; la reserva valida RN-08 y RN-09
                confirmacionService.autorizarConfirmacion(reserva, folio, alojamiento, ahora, autor);

                reservaRepository.guardar(reserva);
                notificador.reservaConfirmada(reserva);   // 7.8
                return reserva;
        }
}
