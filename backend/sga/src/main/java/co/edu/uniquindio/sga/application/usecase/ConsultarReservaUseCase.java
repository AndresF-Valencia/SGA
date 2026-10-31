package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

public class ConsultarReservaUseCase {

    private final ReservaRepository reservaRepository;

    public ConsultarReservaUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    /**
     * Consulta una reserva permitiendo buscar por su código interno estructurado (ej. RES-2026-00001)
     * o por el código externo asignado por una OTA/Canal (ej. BKG-998877).
     */
    public Reserva ejecutar(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            throw new IllegalArgumentException("El identificador de la reserva no puede estar vacío");
        }

        // Si empieza con el prefijo interno "RES-", validamos y usamos el Value Object CodigoReserva
        if (identificador.startsWith("RES-")) {
            CodigoReserva codigoInterno = new CodigoReserva(identificador); // Aplica la validación del formato del VO
            return reservaRepository.buscarPorCodigo(codigoInterno)
                    .orElseThrow(() -> new ReservaNoEncontradaException(codigoInterno));
        } 

        // Si no, se procesa como un código externo (canal de terceros / Booking)
        return reservaRepository.buscarPorCodigoExterno(identificador)
                .orElseThrow(() -> new ReservaNoEncontradaException(identificador));
    }

    /**
     * 7.2: igual que {@link #ejecutar}, pero el huésped solo ve reservas de las que es titular.
     * Se verifica sobre la reserva ya cargada para cubrir también los códigos externos.
     */
    public Reserva ejecutarComoHuesped(String identificador, DocumentoIdentidad documentoHuesped) {
        Reserva reserva = ejecutar(identificador);
        if (!reserva.getTitular().getDocumento().equals(documentoHuesped)) {
            throw new AccesoDenegadoException("La reserva " + identificador + " no pertenece a este usuario");
        }
        return reserva;
    }
}