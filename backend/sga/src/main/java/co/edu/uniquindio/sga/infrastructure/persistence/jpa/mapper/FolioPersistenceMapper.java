package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CargoFolioEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PagoEmbeddable;

import java.util.ArrayList;
import java.util.List;

public final class FolioPersistenceMapper {

    private FolioPersistenceMapper() {
    }

    public static FolioEntity aEntity(Folio folio) {
        FolioEntity entity = new FolioEntity();
        entity.setReservaCodigo(folio.getReserva().valor());
        entity.setCerrado(folio.isCerrado());

        AutorizacionCierre autorizacion = folio.getAutorizacionCierre();
        if (autorizacion != null) {
            entity.setAutorizacionAutor(autorizacion.autor());
            entity.setAutorizacionMotivo(autorizacion.motivo());
            entity.setAutorizacionFecha(autorizacion.fecha());
        }

        List<CargoFolioEmbeddable> cargos = new ArrayList<>();
        for (Cargo cargo : folio.getCargos()) {
            cargos.add(new CargoFolioEmbeddable(cargo.valor().valor(), cargo.descripcion(),
                    cargo.tipo(), cargo.fecha()));
        }
        entity.setCargos(cargos);

        List<PagoEmbeddable> pagos = new ArrayList<>();
        for (Pago pago : folio.getPagos()) {
            pagos.add(new PagoEmbeddable(pago.getMonto().valor(), pago.getMedioPago(), pago.getHoraPago()));
        }
        entity.setPagos(pagos);
        return entity;
    }

    /**
     * Rearma el folio con sus propios métodos, en el mismo orden en que ocurrió:
     * se abre con el primer cargo (el de alojamiento), se agregan los demás
     * cargos y los pagos y, si estaba cerrado, se cierra igual que antes.
     */
    public static Folio aDominio(FolioEntity entity) {
        List<Cargo> cargos = new ArrayList<>();
        for (CargoFolioEmbeddable fila : entity.getCargos()) {
            cargos.add(new Cargo(new Dinero(fila.getValor()), fila.getDescripcion(), fila.getTipo(), fila.getFecha()));
        }

        Folio folio = new Folio(new CodigoReserva(entity.getReservaCodigo()), cargos.get(0));
        for (Cargo cargo : cargos.subList(1, cargos.size())) {
            folio.registrarCargo(cargo);
        }
        for (PagoEmbeddable fila : entity.getPagos()) {
            folio.registrarPago(new Pago(new Dinero(fila.getMonto()), fila.getMedioPago(), fila.getHoraPago()));
        }

        if (entity.isCerrado()) {
            if (entity.getAutorizacionAutor() != null) {
                folio.cerrarConAutorizacion(new AutorizacionCierre(
                        entity.getAutorizacionAutor(), entity.getAutorizacionMotivo(), entity.getAutorizacionFecha()));
            } else {
                folio.cerrar();
            }
        }
        return folio;
    }
}
