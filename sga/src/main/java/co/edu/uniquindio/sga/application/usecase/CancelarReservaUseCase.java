package co.edu.uniquindio.sga.application.usecase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.service.CancelacionReservaService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;

/** Caso de uso (Guía 06), servicio de Spring con transacción (Guía 09) */
@Service
public class CancelarReservaUseCase {

        private final ReservaRepository reservaRepository;
        private final FolioRepository folioRepository;
        private final PoliticaCancelacion politicaCancelacion;
        private final CancelacionReservaService cancelacionReservaService;
        private final AlojamientoRepository alojamientoRepository;

        public CancelarReservaUseCase(ReservaRepository reservaRepository, FolioRepository folioRepository,
                        PoliticaCancelacion politicaCancelacion,
                        CancelacionReservaService cancelacionReservaService,
                        AlojamientoRepository alojamientoRepository) {
                this.reservaRepository = reservaRepository;
                this.folioRepository = folioRepository;
                this.politicaCancelacion = politicaCancelacion;
                this.cancelacionReservaService = cancelacionReservaService;
                this.alojamientoRepository = alojamientoRepository;
        }

        @Transactional
        public void ejecutar(CodigoReserva codigoReserva, IdentificacionAlojamiento identificacionAlojamiento,
                        String motivo, LocalDateTime momentoActual) {
                Reserva reserva = reservaRepository.buscarPorCodigo(codigoReserva)
                                .orElseThrow(() -> new ReservaNoEncontradaException(codigoReserva));

                Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                                .orElseThrow(() -> new FolioNoEncontradoException(codigoReserva));

                Alojamiento alojamiento = alojamientoRepository.buscarPorId(identificacionAlojamiento)
                                .orElseThrow(() -> new AlojamientoNoEncontradoException(identificacionAlojamiento));

                /**
                 * la clase internamente se encaga de calcular la retencion segun la politica de
                 * cancelacion y cancelar la reserva
                 */
                cancelacionReservaService.cancelar(reserva, folio, politicaCancelacion, alojamiento,
                                motivo, reserva.getTitular().getNombre(), momentoActual);

                reservaRepository.guardar(reserva);
                folioRepository.guardar(folio);
                alojamientoRepository.guardar(alojamiento);
        }
}