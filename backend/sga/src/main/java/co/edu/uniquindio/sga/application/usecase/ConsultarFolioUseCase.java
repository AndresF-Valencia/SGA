package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

public class ConsultarFolioUseCase {

    private final FolioRepository folioRepository;
    private final ReservaRepository reservaRepository;

    public ConsultarFolioUseCase(FolioRepository folioRepository, ReservaRepository reservaRepository) {
        this.folioRepository = folioRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Consulta el folio financiero aceptando tanto el código interno (RES-YYYY-NNNNN)
     * como el código externo de un canal (Booking/OTAs).
     */
    public Folio ejecutar(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("El identificador es obligatorio para consultar el folio");
        }

        CodigoReserva codigoReserva;

        // 1. Resolver el CodigoReserva interno según el tipo de identificador recibido
        if (identificador.startsWith("RES-")) {
            codigoReserva = new CodigoReserva(identificador);
        } else {
            Reserva reserva = reservaRepository.buscarPorCodigoExterno(identificador)
                    .orElseThrow(() -> new ReservaNoEncontradaException(identificador));
            codigoReserva = reserva.getCodigo();
        }

        // 2. Buscar y retornar el folio utilizando el código interno resuelto
        return folioRepository.buscarPorReserva(codigoReserva)
                .orElseThrow(() -> new FolioNoEncontradoException(codigoReserva));
    }
}