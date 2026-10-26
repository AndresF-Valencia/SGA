package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CanalEntity;

public final class CanalPersistenceMapper {

    private CanalPersistenceMapper() {
    }

    public static CanalEntity aEntity(Canal canal) {
        CanalEntity entity = new CanalEntity();
        entity.setId(canal.getIdentificacion().identificacion());
        entity.setTipoCanal(canal.getTipoCanal());
        entity.setNombre(canal.getNombre());
        entity.setCredencialAcceso(canal.getCredencialAcceso());
        entity.setActivo(canal.isActivo());
        return entity;
    }

    public static Canal aDominio(CanalEntity entity) {
        Canal canal = new Canal(
                new IdentificacionCanal(entity.getId()),
                entity.getTipoCanal(),
                entity.getNombre(),
                entity.getCredencialAcceso());
        if (!entity.isActivo()) {
            canal.desactivar();
        }
        return canal;
    }
}
