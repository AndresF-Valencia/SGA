package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.BloqueoPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BloqueoRepositoryJpa implements BloqueoRepository {

    private final BloqueoJpaDataRepository jpa;

    public BloqueoRepositoryJpa(BloqueoJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Bloqueo bloqueo) {
        if (bloqueo == null) {
            throw new IllegalArgumentException("No se puede guardar un bloqueo nulo");
        }
        jpa.save(BloqueoPersistenceMapper.aEntity(bloqueo));
    }

    @Override
    public Optional<Bloqueo> buscarPorId(IdentificacionBloqueo id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpa.findById(id.valor()).map(BloqueoPersistenceMapper::aDominio);
    }

    /** RN-07: solo los vigentes quitan disponibilidad. */
    @Override
    public List<Bloqueo> buscarVigentesPorApartamento(IdentificacionApartamento apartamento) {
        return jpa.findByApartamentoIdAndEstado(apartamento.valor(), EstadoBloqueo.VIGENTE).stream()
                .map(BloqueoPersistenceMapper::aDominio)
                .toList();
    }

    @Override
    public List<Bloqueo> buscarPorApartamento(IdentificacionApartamento apartamento) {
        return jpa.findByApartamentoId(apartamento.valor()).stream()
                .map(BloqueoPersistenceMapper::aDominio)
                .toList();
    }

    @Override
    public List<Bloqueo> obtenerTodos() {
        return jpa.findAll().stream().map(BloqueoPersistenceMapper::aDominio).toList();
    }
}
