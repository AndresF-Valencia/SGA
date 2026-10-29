package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador en memoria del puerto {@link ReservaRepository}.
 *
 * Ya no lleva @Repository: la aplicación guarda las reservas con
 * ReservaRepositoryJpa. Esta clase se conserva para las pruebas unitarias,
 * que la crean con new y no necesitan base de datos.
 */
public class ReservaRepositoryEnMemoria extends AlmacenEnMemoria<CodigoReserva, Reserva>
        implements ReservaRepository {

    public ReservaRepositoryEnMemoria() {
        super(Reserva::getCodigo);
    }

    @Override
    public void guardar(Reserva reserva) {
        almacenar(reserva);
    }

    @Override
    public Optional<Reserva> buscarPorCodigo(CodigoReserva codigo) {
        return porId(codigo);
    }

    @Override
    public Optional<Reserva> buscarPorCodigoExterno(String codigoExterno) {
        if (codigoExterno == null || codigoExterno.isBlank()) {
            return Optional.empty();
        }
        return filtrar(r -> r.getCodigoExterno() != null && r.getCodigoExterno().equals(codigoExterno))
                .stream()
                .findFirst();
    }

    @Override
    public List<Reserva> buscarActivasPorApartamento(IdentificacionApartamento apartamento) {
        return filtrar(r -> r.getApartamento().equals(apartamento) && r.getEstado().retieneDisponibilidad());
    }

    @Override
    public List<Reserva> buscarPendientes() {
        return filtrar(r -> r.getEstado() == EstadoReserva.PENDIENTE);
    }

    @Override
    public List<Reserva> obtenerTodas() {
        return todos();
    }
}