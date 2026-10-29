package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.EventoReserva;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.NotificacionRetraso;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CargoNocheEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EventoReservaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.OcupanteEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Traduce entre la Reserva del dominio y la ReservaEntity de la base de datos.
 *
 * Es el único lugar que conoce las dos formas. El dominio no sabe que existe
 * una tabla, y la tabla no sabe nada de reglas de negocio.
 */
public final class ReservaPersistenceMapper {

    private ReservaPersistenceMapper() {
        // Solo métodos estáticos: no se crean instancias.
    }

    // ---------- Dominio → Entity (al guardar) ----------

    public static ReservaEntity aEntity(Reserva reserva) {
        ReservaEntity entity = new ReservaEntity();
        entity.setCodigo(reserva.getCodigo().valor());
        entity.setCodigoExterno(reserva.getCodigoExterno()); 
        entity.setApartamentoId(reserva.getApartamento().valor());
        entity.setEstado(reserva.getEstado());
        entity.setEstancia(new EstanciaEmbeddable(
                reserva.getEstancia().fechaEntrada(),
                reserva.getEstancia().fechaSalida()));
        entity.setCanalOrigen(reserva.getCanalOrigen());
        entity.setCreadaEn(reserva.getCreadaEn());
        entity.setDocumentoTitular(reserva.getTitular().getDocumento().numero());
        entity.setVersionPolitica(reserva.getPolitica().numero());
        entity.setCantidadMascotas(reserva.getCantidadMascotas().cantidad());

        // Opcionales: si el valor del dominio es null, la columna queda vacía
        if (reserva.getHoraEstimadaLlegada() != null) {
            entity.setHoraEstimadaLlegada(reserva.getHoraEstimadaLlegada().horaEstimada());
        }
        NotificacionRetraso retraso = reserva.getNotificacionRetraso();
        if (retraso != null) {
            entity.setFechaNotificacionRetraso(retraso.fechaHoraNotificacion());
            entity.setNuevaHoraEstimada(retraso.nuevaHoraEstimada());
            entity.setMotivoRetraso(retraso.motivo());
        }

        if (reserva.getCorreoContacto() != null) {
            entity.setCorreoContacto(reserva.getCorreoContacto().valor());
        }

        // Listas: cada elemento del dominio se vuelve una fila de su tabla
        List<OcupanteEmbeddable> ocupantes = new ArrayList<>();
        for (Ocupante ocupante : reserva.getOcupantes()) {
            ocupantes.add(new OcupanteEmbeddable(
                    ocupante.getDocumento().numero(),
                    ocupante.getNombre(),
                    ocupante.getFechaNacimiento()));
        }
        entity.setOcupantes(ocupantes);

        List<CargoNocheEmbeddable> cargos = new ArrayList<>();
        for (CargoNoche cargo : reserva.getValor().detalle()) {
            cargos.add(new CargoNocheEmbeddable(
                    cargo.noche(),
                    cargo.temporada(),
                    cargo.tarifaPorOcupante().valor(),
                    cargo.ocupantesFacturables()));
        }
        entity.setCargosPorNoche(cargos);

        List<EventoReservaEmbeddable> historial = new ArrayList<>();
        for (EventoReserva evento : reserva.obtenerHistorial()) {
            historial.add(new EventoReservaEmbeddable(
                    evento.momento(),
                    evento.accion(),
                    evento.autor(),
                    evento.estadoAnterior(),
                    evento.estadoNuevo(),
                    evento.observacion()));
        }
        entity.setHistorial(historial);

        return entity;
    }

    // ---------- Entity → Dominio (al leer) ----------

    public static Reserva aDominio(ReservaEntity entity) {
        // Ocupante pide la "fecha actual" solo para validar que no nació en el futuro.
        // Se usa la fecha de creación de la reserva: así leer da siempre el mismo resultado.
        List<Ocupante> ocupantes = new ArrayList<>();
        for (OcupanteEmbeddable fila : entity.getOcupantes()) {
            ocupantes.add(new Ocupante(
                    new DocumentoIdentidad(fila.getDocumento()),
                    fila.getNombre(),
                    fila.getFechaNacimiento(),
                    entity.getCreadaEn().toLocalDate()));
        }

        // El titular es uno de los ocupantes: se busca por su documento
        Ocupante titular = ocupantes.stream()
                .filter(o -> o.getDocumento().numero().equals(entity.getDocumentoTitular()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "La reserva " + entity.getCodigo() + " tiene un titular que no está entre sus ocupantes"));

        List<CargoNoche> cargos = new ArrayList<>();
        for (CargoNocheEmbeddable fila : entity.getCargosPorNoche()) {
            cargos.add(new CargoNoche(
                    fila.getNoche(),
                    fila.getTemporada(),
                    new Dinero(fila.getTarifaPorOcupante()),
                    fila.getOcupantesFacturables()));
        }

        List<EventoReserva> historial = new ArrayList<>();
        for (EventoReservaEmbeddable fila : entity.getHistorial()) {
            historial.add(new EventoReserva(
                    fila.getMomento(),
                    fila.getAccion(),
                    fila.getAutor(),
                    fila.getEstadoAnterior(),
                    fila.getEstadoNuevo(),
                    fila.getObservacion()));
        }

        HoraEstimadaLlegada hora = entity.getHoraEstimadaLlegada() == null
                ? null
                : new HoraEstimadaLlegada(entity.getHoraEstimadaLlegada());

        NotificacionRetraso retraso = entity.getFechaNotificacionRetraso() == null
                ? null
                : new NotificacionRetraso(
                        entity.getFechaNotificacionRetraso(),
                        entity.getNuevaHoraEstimada(),
                        entity.getMotivoRetraso());

        Reserva reserva = Reserva.reconstruir(
                new CodigoReserva(entity.getCodigo()),
                entity.getCodigoExterno(), // 👈 Se pasa el código externo reconstruido
                new IdentificacionApartamento(entity.getApartamentoId()),
                new Estancia(entity.getEstancia().getFechaEntrada(), entity.getEstancia().getFechaSalida()),
                titular,
                ocupantes,
                entity.getCanalOrigen(),
                new ValorCongelado(cargos),
                new VersionPolitica(entity.getVersionPolitica()),
                entity.getCreadaEn(),
                new CantidadMascota(entity.getCantidadMascotas()),
                entity.getEstado(),
                hora,
                retraso,
                historial);
        if (entity.getCorreoContacto() != null) {
            reserva.registrarCorreoContacto(new Correo(entity.getCorreoContacto()));
        }
        return reserva;
    }
}