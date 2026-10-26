package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TramoCancelacionEmbeddable;

import java.util.ArrayList;
import java.util.List;

public final class PoliticaCancelacionPersistenceMapper {

    private PoliticaCancelacionPersistenceMapper() {
    }

    public static PoliticaCancelacionEntity aEntity(VersionPolitica version, PoliticaCancelacion politica) {
        PoliticaCancelacionEntity entity = new PoliticaCancelacionEntity();
        entity.setVersion(version.numero());
        entity.setNombre(politica.nombre());

        List<TramoCancelacionEmbeddable> tramos = new ArrayList<>();
        for (TramoCancelacion tramo : politica.tramos()) {
            tramos.add(new TramoCancelacionEmbeddable(tramo.diasMinimosAnticipacion(),
                    tramo.diasMaximosAnticipacion(), tramo.porcentajeDevolucion()));
        }
        entity.setTramos(tramos);
        return entity;
    }

    public static PoliticaCancelacion aDominio(PoliticaCancelacionEntity entity) {
        List<TramoCancelacion> tramos = new ArrayList<>();
        for (TramoCancelacionEmbeddable fila : entity.getTramos()) {
            tramos.add(new TramoCancelacion(fila.getDiasMinimosAnticipacion(),
                    fila.getDiasMaximosAnticipacion(), fila.getPorcentajeDevolucion()));
        }
        return new PoliticaCancelacion(entity.getNombre(), tramos);
    }
}
