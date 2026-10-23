package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstanciaMinima;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTarifa;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionTemporada;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import co.edu.uniquindio.sga.domain.valueobject.TramoCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos de prueba compartidos por las pruebas de los servicios de dominio.
 *
 * El alojamiento recibe a las 15:00 y entrega a las 11:00 (4 horas entre una
 * salida y una entrada), cobra 100.000 por persona y noche en temporada base y
 * 150.000 en temporada alta, y la temporada alta exige 3 noches.
 */
public final class Escenario {

    private Escenario() {
    }

    /** Momento fijo: las pruebas no dependen del reloj real. */
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 12, 10, 0);
    public static final String RECEPCION = "recepcionista";

    public static final IdentificacionAlojamiento SAN_ROCCO = new IdentificacionAlojamiento("SAN-ROCCO");
    public static final IdentificacionApartamento APTO_101 = new IdentificacionApartamento("APTO-101");
    public static final IdentificacionApartamento APTO_102 = new IdentificacionApartamento("APTO-102");

    /** Del 1 al 4 de noviembre: 3 noches en temporada base. */
    public static final Estancia NOVIEMBRE = new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4));

    public static final Temporada BASE = new Temporada(new IdentificacionTemporada("T-BASE"),
            new Periodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 14)),
            "Base", TipoTemporada.BASE, null);
    public static final Temporada ALTA = new Temporada(new IdentificacionTemporada("T-ALTA"),
            new Periodo(LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15)),
            "Alta de fin de año", TipoTemporada.ALTA, new EstanciaMinima(3));
    public static final List<Temporada> TEMPORADAS = List.of(BASE, ALTA);

    public static final List<Tarifa> TARIFAS = List.of(
            new Tarifa(new IdentificacionTarifa("TF-1"), APTO_101, BASE.getIdentificacion(), Dinero.de(100_000)),
            new Tarifa(new IdentificacionTarifa("TF-2"), APTO_101, ALTA.getIdentificacion(), Dinero.de(150_000)));

    public static final PoliticaCancelacion POLITICA = new PoliticaCancelacion("Estándar", List.of(
            new TramoCancelacion(0, 6, 0),
            new TramoCancelacion(7, 29, 50),
            new TramoCancelacion(30, 365, 100)));

    public static Alojamiento alojamiento() {
        return alojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0), true, 50);
    }

    public static Alojamiento alojamiento(LocalTime horaEntrada, LocalTime horaSalida,
                                   boolean exigeAnticipo, int porcentajeAnticipo) {
        return new Alojamiento(SAN_ROCCO, "San Rocco", "Armenia", "Apartamentos turísticos",
                horaEntrada, horaSalida, LocalTime.of(20, 0), new UmbralEdadFacturable(3),
                TiempoPreparacion.estandar(), PlazoConfirmacion.unaHora(), exigeAnticipo, porcentajeAnticipo);
    }

    /** Apartamento para 4 personas y hasta 1 mascota, con 3 horas de preparación. */
    public static Apartamento apartamento(IdentificacionApartamento id) {
        return new Apartamento(id, "Apartamento " + id.valor(), TiempoPreparacion.estandar(), SAN_ROCCO,
                new Dormitorio(2), new CapacidadOcupantes(4), new LimiteMascota(1),
                List.of("Wifi", "Balcón"), List.of("https://img.sga.com/apto-default.jpg"));
    }

    public static Ocupante adulto(String documento, String nombre) {
        return new Ocupante(new DocumentoIdentidad(documento), nombre,
                LocalDate.of(1990, 5, 1), AHORA.toLocalDate());
    }

    /** Un niño de 2 años: ocupa cupo pero no paga (umbral de 3 años). */
    public static Ocupante nino(String documento, String nombre) {
        return new Ocupante(new DocumentoIdentidad(documento), nombre,
                LocalDate.of(2024, 6, 1), AHORA.toLocalDate());
    }

    public static final Ocupante LAURA = adulto("1094000001", "Laura Gómez");
    public static final Ocupante PEDRO = adulto("1094000002", "Pedro Gómez");

    /** Desglose de 100.000 por ocupante y noche (temporada base). */
    public static ValorCongelado valorBase(Estancia estancia, int ocupantes) {
        return new ValorCongelado(estancia.nochesOcupadas().stream()
                .map(noche -> new CargoNoche(noche, "Base", Dinero.de(100_000), ocupantes))
                .toList());
    }

    /** Reserva PENDIENTE de Laura y Pedro en el APTO-101, sin mascotas. */
    public static Reserva reserva(String codigo, Estancia estancia) {
        return reserva(codigo, APTO_101, estancia);
    }

    public static Reserva reserva(String codigo, IdentificacionApartamento apartamento, Estancia estancia) {
        return Reserva.crear(codigo(codigo), apartamento, estancia, LAURA, List.of(LAURA, PEDRO),
                CanalOrigen.DIRECTO, valorBase(estancia, 2), new VersionPolitica(1), AHORA, new CantidadMascota(0));
    }

    /** Convierte un código corto de prueba ("RES-7") al formato real: RES-2026-00007. */
    public static CodigoReserva codigo(String corto) {
        int numero = Integer.parseInt(corto.substring(corto.lastIndexOf('-') + 1));
        return new CodigoReserva(String.format("RES-2026-%05d", numero));
    }

    /** Reserva ya CONFIRMADA (con hora de llegada indicada). */
    public static Reserva reservaConfirmada(String codigo, Estancia estancia) {
        Reserva reserva = reserva(codigo, estancia);
        reserva.indicarHoraEstimadaLlegada(LocalTime.of(15, 0), RECEPCION);
        reserva.confirmar(RECEPCION, AHORA);
        return reserva;
    }

    /** Folio abierto con el cargo de alojamiento por el valor de la reserva. */
    public static Folio folio(Reserva reserva) {
        return new Folio(reserva.getCodigo(), new Cargo(reserva.getValor().total(),
                "Alojamiento", TipoCargo.ALOJAMIENTO, AHORA));
    }

    public static Pago pago(long pesos) {
        return new Pago(Dinero.de(pesos), MedioPago.EFECTIVO, AHORA);
    }
}