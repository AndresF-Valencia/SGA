package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PeriodoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TemporadaEntity;

public final class TemporadaPersistenceMapper {

    private TemporadaPersistenceMapper() {
    }

    public static TemporadaEntity aEntity(Temporada temporada) {
        TemporadaEntity entity = new TemporadaEntity();
        entity.setId(temporada.getIdentificacion().id());
        entity.setPeriodo(new PeriodoEmbeddable(temporada.getPeriodo().desde(), temporada.getPeriodo().hasta()));
        entity.setNombre(temporada.getNombre());
        entity.setTipo(temporada.getTipo());
        entity.setEstanciaMinimaNoches(
                temporada.getEstanciaMinima() == null ? null : temporada.getEstanciaMinima().noches());
        return entity;
    }

    public static Temporada aDominio(TemporadaEntity entity) {
        return new Temporada(
                new IdentificacionTemporada(entity.getId()),
                new Periodo(entity.getPeriodo().getDesde(), entity.getPeriodo().getHasta()),
                entity.getNombre(),
                entity.getTipo(),
                entity.getEstanciaMinimaNoches() == null ? null : new EstanciaMinima(entity.getEstanciaMinimaNoches()));
    }
}
