package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

/**
 * 7.2: un huésped NUNCA accede a reservas ni folios de otros huéspedes.
 * La regla se verifica sobre el PROPIETARIO del recurso, no solo sobre el rol:
 * el huésped solo pasa si es el titular de la reserva.
 * Los roles internos (recepción, administración) no se verifican aquí.
 */
public class VerificarAccesoReservaUseCase {

    private final ReservaRepository reservaRepository;

    public VerificarAccesoReservaUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public void verificarTitular(CodigoReserva codigo, DocumentoIdentidad documentoHuesped) {
        Reserva reserva = reservaRepository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ReservaNoEncontradaException(codigo));
        if (!reserva.getTitular().getDocumento().equals(documentoHuesped)) {
            throw new AccesoDenegadoException("La reserva " + codigo.valor() + " no pertenece a este usuario");
        }
    }
}
