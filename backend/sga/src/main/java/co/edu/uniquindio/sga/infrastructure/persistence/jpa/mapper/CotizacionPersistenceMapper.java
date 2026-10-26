package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCotizacion;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CargoNocheEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CotizacionEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EstanciaEmbeddable;

import java.util.ArrayList;
import java.util.List;

public final class CotizacionPersistenceMapper {

    private CotizacionPersistenceMapper() {
    }

    public static CotizacionEntity aEntity(Cotizacion cotizacion) {
        CotizacionEntity entity = new CotizacionEntity();
        entity.setId(cotizacion.getIdentificacion().valor());
        entity.setApartamentoId(cotizacion.getIdentificacionApartamento().valor());
        entity.setEstancia(new EstanciaEmbeddable(
                cotizacion.getEstancia().fechaEntrada(), cotizacion.getEstancia().fechaSalida()));
        entity.setOcupantesFacturables(cotizacion.getOcupantesFacturables());
        entity.setCantidadMascotas(cotizacion.getCantidadMascota().cantidad());
        entity.setCreadaEn(cotizacion.getCreadaEn());

        List<CargoNocheEmbeddable> cargos = new ArrayList<>();
        for (CargoNoche cargo : cotizacion.getValor().detalle()) {
            cargos.add(new CargoNocheEmbeddable(cargo.noche(), cargo.temporada(),
                    cargo.tarifaPorOcupante().valor(), cargo.ocupantesFacturables()));
        }
        entity.setCargosPorNoche(cargos);
        return entity;
    }

    public static Cotizacion aDominio(CotizacionEntity entity) {
        List<CargoNoche> cargos = new ArrayList<>();
        for (CargoNocheEmbeddable fila : entity.getCargosPorNoche()) {
            cargos.add(new CargoNoche(fila.getNoche(), fila.getTemporada(),
                    new Dinero(fila.getTarifaPorOcupante()), fila.getOcupantesFacturables()));
        }
        return new Cotizacion(
                new IdentificacionCotizacion(entity.getId()),
                new IdentificacionApartamento(entity.getApartamentoId()),
                new Estancia(entity.getEstancia().getFechaEntrada(), entity.getEstancia().getFechaSalida()),
                entity.getOcupantesFacturables(),
                new CantidadMascota(entity.getCantidadMascotas()),
                new ValorCongelado(cargos),
                entity.getCreadaEn());
    }
}
