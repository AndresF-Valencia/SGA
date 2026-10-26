package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.Novedad;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ApartamentoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadEmbeddable;

import java.util.ArrayList;
import java.util.List;

public final class ApartamentoPersistenceMapper {

    private ApartamentoPersistenceMapper() {
    }

    public static ApartamentoEntity aEntity(Apartamento apartamento) {
        ApartamentoEntity entity = new ApartamentoEntity();
        entity.setId(apartamento.getIdentificacion().valor());
        entity.setAlojamientoId(apartamento.getIdentificacionAlojamiento().identificacion());
        entity.setNombre(apartamento.getNombre());
        entity.setDormitorios(apartamento.getDormitorios().cantidad());
        entity.setCapacidad(apartamento.getCapacidad().cantidad());
        entity.setEstadoOperativo(apartamento.getEstadoOperativo());
        entity.setLimiteMascotas(apartamento.getLimiteMascotas().limite());
        entity.setTiempoPreparacionHoras(apartamento.getTiempoPreparacion().tiempoPreparacion());
        entity.setActivo(apartamento.estaActivo());
        entity.setCaracteristicas(new ArrayList<>(apartamento.getCaracteristicas()));
        entity.setImagenes(new ArrayList<>(apartamento.getImagenes()));

        List<NovedadEmbeddable> novedades = new ArrayList<>();
        for (Novedad novedad : apartamento.getNovedades()) {
            novedades.add(new NovedadEmbeddable(
                    novedad.descripcion(), novedad.gravedad(), novedad.autor(), novedad.fecha()));
        }
        entity.setNovedades(novedades);
        return entity;
    }

    public static Apartamento aDominio(ApartamentoEntity entity) {
        List<Novedad> novedades = new ArrayList<>();
        for (NovedadEmbeddable fila : entity.getNovedades()) {
            novedades.add(Novedad.de(fila.getDescripcion(), fila.getGravedad(), fila.getAutor(), fila.getFecha()));
        }
        return Apartamento.reconstruir(
                new IdentificacionApartamento(entity.getId()),
                entity.getNombre(),
                new TiempoPreparacion(entity.getTiempoPreparacionHoras()),
                new IdentificacionAlojamiento(entity.getAlojamientoId()),
                new Dormitorio(entity.getDormitorios()),
                new CapacidadOcupantes(entity.getCapacidad()),
                new LimiteMascota(entity.getLimiteMascotas()),
                entity.getCaracteristicas(),
                entity.getImagenes(),
                entity.getEstadoOperativo(),
                entity.isActivo(),
                novedades);
    }
}
