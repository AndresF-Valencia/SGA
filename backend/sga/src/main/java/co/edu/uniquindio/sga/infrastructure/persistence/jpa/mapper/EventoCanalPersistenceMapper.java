package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.EventoCanal;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EventoCanalEntity;

public final class EventoCanalPersistenceMapper {

    private EventoCanalPersistenceMapper() {
    }

    public static EventoCanalEntity aEntity(EventoCanal evento) {
        EventoCanalEntity e = new EventoCanalEntity();
        e.setId(evento.getId());
        e.setCanal(evento.getCanal());
        e.setOperacion(evento.getOperacion());
        e.setCodigoExterno(evento.getCodigoExterno());
        e.setExitoso(evento.fueExitoso());
        e.setDetalle(evento.getDetalle());
        e.setOcurridoEn(evento.getOcurridoEn());
        return e;
    }

    public static EventoCanal aDominio(EventoCanalEntity e) {
        return EventoCanal.registrar(e.getId(), e.getCanal(), e.getOperacion(), e.getCodigoExterno(),
                e.isExitoso(), e.getDetalle(), e.getOcurridoEn());
    }
}
