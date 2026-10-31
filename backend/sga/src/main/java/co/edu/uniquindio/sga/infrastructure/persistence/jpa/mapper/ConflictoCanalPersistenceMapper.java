package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.ConflictoCanal;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ConflictoCanalEntity;

public final class ConflictoCanalPersistenceMapper {

    private ConflictoCanalPersistenceMapper() {
    }

    public static ConflictoCanalEntity aEntity(ConflictoCanal c) {
        ConflictoCanalEntity e = new ConflictoCanalEntity();
        e.setId(c.getId());
        e.setCanal(c.getCanal());
        e.setCodigoExterno(c.getCodigoExterno());
        e.setApartamento(c.getApartamento());
        e.setFechaEntrada(c.getFechaEntrada());
        e.setFechaSalida(c.getFechaSalida());
        e.setTitularNombre(c.getTitularNombre());
        e.setTitularDocumento(c.getTitularDocumento());
        e.setMotivo(c.getMotivo());
        e.setRecibidoEn(c.getRecibidoEn());
        e.setResuelto(c.estaResuelto());
        e.setResolucion(c.getResolucion());
        e.setResueltoPor(c.getResueltoPor());
        e.setResueltoEn(c.getResueltoEn());
        return e;
    }

    public static ConflictoCanal aDominio(ConflictoCanalEntity e) {
        return ConflictoCanal.reconstruir(e.getId(), e.getCanal(), e.getCodigoExterno(), e.getApartamento(),
                e.getFechaEntrada(), e.getFechaSalida(), e.getTitularNombre(), e.getTitularDocumento(),
                e.getMotivo(), e.getRecibidoEn(), e.isResuelto(), e.getResolucion(), e.getResueltoPor(),
                e.getResueltoEn());
    }
}
