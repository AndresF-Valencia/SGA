package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PeriodoEmbeddable;

public final class BloqueoPersistenceMapper {

    private BloqueoPersistenceMapper() {
    }

    public static BloqueoEntity aEntity(Bloqueo bloqueo) {
        BloqueoEntity entity = new BloqueoEntity();
        entity.setId(bloqueo.getIdentificacionBloqueo().valor());
        entity.setApartamentoId(bloqueo.getApartamento().valor());
        entity.setPeriodo(new PeriodoEmbeddable(bloqueo.getPeriodo().desde(), bloqueo.getPeriodo().hasta()));
        entity.setMotivo(bloqueo.getMotivo());
        entity.setEstado(bloqueo.getEstado());
        return entity;
    }

    /** Todo bloqueo nace VIGENTE; si estaba levantado, se levanta con el método del dominio. */
    public static Bloqueo aDominio(BloqueoEntity entity) {
        Bloqueo bloqueo = new Bloqueo(
                new IdentificacionBloqueo(entity.getId()),
                new IdentificacionApartamento(entity.getApartamentoId()),
                new Periodo(entity.getPeriodo().getDesde(), entity.getPeriodo().getHasta()),
                entity.getMotivo());
        if (entity.getEstado() == EstadoBloqueo.LEVANTADO) {
            bloqueo.levantar();
        }
        return bloqueo;
    }
}
