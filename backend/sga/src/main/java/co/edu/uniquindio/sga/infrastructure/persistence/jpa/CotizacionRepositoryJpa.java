package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.entity.Cotizacion;
import co.edu.uniquindio.sga.domain.repository.CotizacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCotizacion;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.CotizacionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CotizacionRepositoryJpa implements CotizacionRepository {

    private final CotizacionJpaDataRepository jpa;

    public CotizacionRepositoryJpa(CotizacionJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Cotizacion cotizacion) {
        if (cotizacion == null) {
            throw new IllegalArgumentException("No se puede guardar una cotización nula");
        }
        jpa.save(CotizacionPersistenceMapper.aEntity(cotizacion));
    }

    @Override
    public Optional<Cotizacion> buscarPorId(IdentificacionCotizacion id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpa.findById(id.valor()).map(CotizacionPersistenceMapper::aDominio);
    }
}
