package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ReservaPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA del puerto ReservaRepository: guarda las reservas en la base de datos.
 *
 * Implementa la MISMA interfaz que ReservaRepositoryEnMemoria. Por eso los casos
 * de uso no cambian: siguen pidiendo un ReservaRepository y Spring les entrega
 * este, porque es el único que tiene @Repository.
 */
@Repository
public class ReservaRepositoryJpa implements ReservaRepository {

    private final ReservaJpaDataRepository jpa;

    public ReservaRepositoryJpa(ReservaJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("No se puede guardar una reserva nula");
        }
        // save inserta si el código no existe y actualiza si ya existe
        jpa.save(ReservaPersistenceMapper.aEntity(reserva));
    }

    @Override
    public Optional<Reserva> buscarPorCodigo(CodigoReserva codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return jpa.findById(codigo.valor()).map(ReservaPersistenceMapper::aDominio);
    }

    /**
     * TODO RN-19: la Reserva todavía no guarda el identificador externo del canal,
     * así que, igual que la versión en memoria, solo filtra por canal.
     */
    @Override
    public Optional<Reserva> buscarPorCanalYIdentificadorExterno(CanalOrigen canalOrigen, String identificadorExterno) {
        return jpa.findFirstByCanalOrigen(canalOrigen).map(ReservaPersistenceMapper::aDominio);
    }

    @Override
    public List<Reserva> buscarActivasPorApartamento(IdentificacionApartamento apartamento) {
        // Los estados que retienen noches (RN-12) los decide el dominio, no esta clase
        List<EstadoReserva> activos = Arrays.stream(EstadoReserva.values())
                .filter(EstadoReserva::retieneDisponibilidad)
                .toList();
        return jpa.findByApartamentoIdAndEstadoIn(apartamento.valor(), activos).stream()
                .map(ReservaPersistenceMapper::aDominio)
                .toList();
    }

    @Override
    public List<Reserva> buscarPendientes() {
        return jpa.findByEstado(EstadoReserva.PENDIENTE).stream()
                .map(ReservaPersistenceMapper::aDominio)
                .toList();
    }

    @Override
    public List<Reserva> obtenerTodas() {
        return jpa.findAll().stream()
                .map(ReservaPersistenceMapper::aDominio)
                .toList();
    }
}
