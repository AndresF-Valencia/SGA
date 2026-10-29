package co.edu.uniquindio.sga.application.usecase;


import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.port.NotificadorReserva;
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
        private final NotificadorReserva notificador;

        public CancelarReservaUseCase(ReservaRepository reservaRepository, FolioRepository folioRepository,
                        PoliticaCancelacionRepository politicaRepository,
                        CancelacionReservaService cancelacionReservaService,
                        AlojamientoRepository alojamientoRepository, NotificadorReserva notificador) {
                this.reservaRepository = reservaRepository;
                this.folioRepository = folioRepository;
                this.politicaRepository = politicaRepository;
                this.cancelacionReservaService = cancelacionReservaService;
                this.alojamientoRepository = alojamientoRepository;
                this.notificador = notificador;
        }

        @Transactional
        public Reserva ejecutar(CodigoReserva codigoReserva, IdentificacionAlojamiento identificacionAlojamiento,
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
                notificador.reservaCancelada(reserva); // 7.8
                return reserva;
        }

        /**
         * Procesa la cancelación originada por un canal externo utilizando su código
         * externo.
         */
        @Transactional
        public Reserva ejecutarPorCodigoExterno(String codigoExterno, String motivo, LocalDateTime momentoActual,
                        String autor) {
                if (codigoExterno == null || codigoExterno.isBlank()) {
                        throw new ReglaDominioException("El código externo es obligatorio para cancelar la reserva");
                }

                // 1. Buscar la reserva por su código externo de la OTA
                Reserva reserva = reservaRepository.buscarPorCodigoExterno(codigoExterno)
                                .orElseThrow(() -> new ReglaDominioException(
                                                "No se encontró ninguna reserva externa con el código: "
                                                                + codigoExterno));

                // 2. Obtener el alojamiento único del sistema (como se maneja un único
                // alojamiento)
                Alojamiento alojamientoUnico = alojamientoRepository.obtenerUnico()
                                .orElseThrow(() -> new AlojamientoNoEncontradoException());

                // 3. Reutilizar tu método principal 'ejecutar' que ya tiene toda la lógica de
                // folios, políticas y notificaciones
                return ejecutar(
                                reserva.getCodigo(),
                                alojamientoUnico.getIdentificacion(),
                                motivo,
                                momentoActual,
                                autor);
        }
}