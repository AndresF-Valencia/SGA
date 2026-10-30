package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.NormasConvivencia;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.AlojamientoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ServicioAdicionalEmbeddable;

public final class AlojamientoPersistenceMapper {

    private AlojamientoPersistenceMapper() {
    }

    public static AlojamientoEntity aEntity(Alojamiento alojamiento) {
        AlojamientoEntity entity = new AlojamientoEntity();
        entity.setId(alojamiento.getIdentificacion().identificacion());
        entity.setNombre(alojamiento.getNombre());
        entity.setCiudad(alojamiento.getCiudad());
        entity.setDescripcion(alojamiento.getDescripcion());
        entity.setDireccion(alojamiento.getDireccion());
        entity.setLatitud(alojamiento.getLatitud());
        entity.setLongitud(alojamiento.getLongitud());
        entity.setNormasConvivencia(alojamiento.getNormasConvivencia().texto());
        for (ServicioAdicional servicio : alojamiento.getServiciosAdicionales()) {
            ServicioAdicionalEmbeddable fila = new ServicioAdicionalEmbeddable();
            fila.setNombre(servicio.nombre());
            fila.setValor(servicio.valor().valor());
            entity.getServiciosAdicionales().add(fila);
        }
        entity.setHoraEntrada(alojamiento.getHoraEntrada());
        entity.setHoraSalida(alojamiento.getHoraSalida());
        entity.setHoraLimiteNoShow(alojamiento.getHoraLimiteNoShow());
        entity.setUmbralEdadFacturable(alojamiento.getUmbralEdadFacturable().anios());
        entity.setTiempoPreparacionHoras(alojamiento.getTiempoPreparacion().tiempoPreparacion());
        entity.setPlazoConfirmacionHoras(alojamiento.getPlazoConfirmacion().horas());
        entity.setExigeAnticipo(alojamiento.isExigeAnticipo());
        entity.setPorcentajeAnticipo(alojamiento.getPorcentajeAnticipo());
        entity.setVersionPoliticaVigente(alojamiento.getVersionPoliticaVigente().numero());
        return entity;
    }

    public static Alojamiento aDominio(AlojamientoEntity entity) {
        Alojamiento alojamiento = Alojamiento.reconstruir(
                new IdentificacionAlojamiento(entity.getId()),
                entity.getNombre(),
                entity.getCiudad(),
                entity.getDescripcion(),
                entity.getHoraEntrada(),
                entity.getHoraSalida(),
                entity.getHoraLimiteNoShow(),
                new UmbralEdadFacturable(entity.getUmbralEdadFacturable()),
                new TiempoPreparacion(entity.getTiempoPreparacionHoras()),
                new PlazoConfirmacion(entity.getPlazoConfirmacionHoras()),
                entity.isExigeAnticipo(),
                entity.getPorcentajeAnticipo(),
                new VersionPolitica(entity.getVersionPoliticaVigente()));
        // Los alojamientos guardados antes de la ficha 7.3 no tienen dirección: se dejan sin ella
        if (entity.getDireccion() != null && entity.getLatitud() != null && entity.getLongitud() != null) {
            alojamiento.actualizarFichaDescriptiva(entity.getDireccion(), entity.getLatitud(),
                    entity.getLongitud(), new NormasConvivencia(entity.getNormasConvivencia()),
                    entity.getServiciosAdicionales().stream()
                            .map(s -> new ServicioAdicional(s.getNombre(), new Dinero(s.getValor())))
                            .toList());
        }
        return alojamiento;
    }
}
