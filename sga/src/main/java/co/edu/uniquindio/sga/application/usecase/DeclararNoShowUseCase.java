package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.NoShowService;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.service.NoShowService;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import java.time.LocalDateTime;

/**
 * Caso de uso (Guía 06): declarar que el titular no se presentó (no-show).
 * Delega en NoShowService, que valida la hora límite y la notificación de
 * retraso
 * (RNP-04), pasa la reserva a NO_SHOW (RN-12) y retiene el anticipo en el folio
 * (Ficha A.4).
 * El alojamiento solo se consulta (porcentaje del anticipo), así que no se
 * guarda.
 * No cierra el folio: eso es otra intención (7.7).
 */

public class DeclararNoShowUseCase {

    private final ReservaRepository reservaRepository;
    private final FolioRepository folioRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final NoShowService noShowService;

    public DeclararNoShowUseCase(ReservaRepository reservaRepository, FolioRepository folioRepository,
            NoShowService noShowService,
            AlojamientoRepository alojamientoRepository) {
        this.reservaRepository = reservaRepository;
        this.folioRepository = folioRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.noShowService = noShowService;
    }

    public Reserva ejecutar(CodigoReserva codigo, IdentificacionAlojamiento alojamientoId,
            String autor, LocalDateTime ahora) {
        // 1. Buscar
        Reserva reserva = reservaRepository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ReservaNoEncontradaException(codigo));

        Folio folio = folioRepository.buscarPorReserva(codigo)
                .orElseThrow(() -> new FolioNoEncontradoException(codigo));
        // 2. Delegar
        Alojamiento alojamiento = alojamientoRepository.buscarPorId(alojamientoId)
                .orElseThrow(() -> new AlojamientoNoEncontradoException(alojamientoId));

        noShowService.declararNoShow(reserva, folio, alojamiento, autor, ahora);

        // 3. Guardar
        reservaRepository.guardar(reserva);
        folioRepository.guardar(folio);
        return reserva;
    }
}
