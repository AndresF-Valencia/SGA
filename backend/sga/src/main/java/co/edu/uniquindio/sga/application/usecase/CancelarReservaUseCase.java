package co.edu.uniquindio.sga.application.usecase;

import java.time.LocalDateTime;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.service.CancelacionReservaService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;

/** Caso de uso (Guía 06). Se vuelve servicio de Spring en la Guía 09. */
public class CancelarReservaUseCase {

        private final ReservaRepository reservaRepository;
        private final FolioRepository folioRepository;
        private final PoliticaCancelacionRepository politicaRepository;
        private final CancelacionReservaService cancelacionReservaService;
        private final AlojamientoRepository alojamientoRepository;

        public CancelarReservaUseCase(ReservaRepository reservaRepository, FolioRepository folioRepository,
                        PoliticaCancelacionRepository politicaRepository,
                        CancelacionReservaService cancelacionReservaService,
                        AlojamientoRepository alojamientoRepository) {
                this.reservaRepository = reservaRepository;
                this.folioRepository = folioRepository;
                this.politicaRepository = politicaRepository;
                this.cancelacionReservaService = cancelacionReservaService;
                this.alojamientoRepository = alojamientoRepository;
        }

        public void ejecutar(CodigoReserva codigoReserva, IdentificacionAlojamiento identificacionAlojamiento,
                        String motivo, LocalDateTime momentoActual, String autor) {
                // 1. BUSCAR
                Reserva reserva = reservaRepository.buscarPorCodigo(codigoReserva)
                                .orElseThrow(() -> new ReservaNoEncontradaException(codigoReserva));

                PoliticaCancelacion politicaCongelada = politicaRepository.buscarPorVersion(reserva.getPolitica())
                                .orElseThrow(() -> new PoliticaCancelacionNoEncontradaException(
                                                "No existe la versión " + reserva.getPolitica().numero()
                                                                + " de la política"));

                Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                                .orElseThrow(() -> new FolioNoEncontradoException(codigoReserva));

                Alojamiento alojamiento = alojamientoRepository.buscarPorId(identificacionAlojamiento)
                                .orElseThrow(() -> new AlojamientoNoEncontradoException(identificacionAlojamiento));

                // 2. DELEGAR
                cancelacionReservaService.cancelar(reserva, folio, politicaCongelada, alojamiento,
                                motivo, autor, momentoActual);

                // 3. GUARDAR
                reservaRepository.guardar(reserva);
                folioRepository.guardar(folio);
        }
}