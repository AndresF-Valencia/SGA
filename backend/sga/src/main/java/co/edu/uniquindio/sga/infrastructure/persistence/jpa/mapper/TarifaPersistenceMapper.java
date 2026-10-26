package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TarifaEntity;

public final class TarifaPersistenceMapper {

    private TarifaPersistenceMapper() {
    }

    public static TarifaEntity aEntity(Tarifa tarifa) {
        TarifaEntity entity = new TarifaEntity();
        entity.setId(tarifa.getIdentificacion().identificacion());
        entity.setApartamentoId(tarifa.getApartamento().valor());
        entity.setTemporadaId(tarifa.getTemporada().id());
        entity.setValorPorOcupanteNoche(tarifa.getValorPorOcupanteNoche().valor());
        return entity;
    }

    public static Tarifa aDominio(TarifaEntity entity) {
        return new Tarifa(
                new IdentificacionTarifa(entity.getId()),
                new IdentificacionApartamento(entity.getApartamentoId()),
                new IdentificacionTemporada(entity.getTemporadaId()),
                new Dinero(entity.getValorPorOcupanteNoche()));
    }
}
