package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.FolioPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FolioRepositoryJpa implements FolioRepository {

    private final FolioJpaDataRepository jpa;

    public FolioRepositoryJpa(FolioJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Folio folio) {
        if (folio == null) {
            throw new IllegalArgumentException("No se puede guardar un folio nulo");
        }
        jpa.save(FolioPersistenceMapper.aEntity(folio));
    }

    @Override
    public Optional<Folio> buscarPorReserva(CodigoReserva reserva) {
        if (reserva == null) {
            return Optional.empty();
        }
        return jpa.findById(reserva.valor()).map(FolioPersistenceMapper::aDominio);
    }
}
