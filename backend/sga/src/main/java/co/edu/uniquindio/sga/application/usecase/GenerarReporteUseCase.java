package co.edu.uniquindio.sga.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.dto.response.ReporteResponse;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: generar el reporte de gestión de un periodo (ocupación, ingresos y reservas).
 *
 * Qué cuenta cada cifra:
 * - Ocupación: noches dentro del periodo de las reservas CONFIRMADA, EN_CURSO y FINALIZADA,
 *   sobre las noches disponibles (días del periodo por apartamentos activos).
 * - Ingresos: pagos registrados en los folios cuya fecha cae dentro del periodo.
 * - Reservas por estado y por canal: las reservas cuya fecha de entrada cae en el periodo.
 * - Valor reservado: valor de esas reservas, sin las canceladas ni los no-show.
 */
public class GenerarReporteUseCase {

    /** Tope del periodo, para que un rango enorme no recorra años de datos. */
    public static final int MAXIMO_DIAS = 366;

    private final ReservaRepository reservaRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final FolioRepository folioRepository;

    public GenerarReporteUseCase(ReservaRepository reservaRepository,
                                 ApartamentoRepository apartamentoRepository,
                                 FolioRepository folioRepository) {
        this.reservaRepository = reservaRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.folioRepository = folioRepository;
    }

    @Transactional(readOnly = true)
    public ReporteResponse ejecutar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas 'desde' y 'hasta' del reporte son obligatorias");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha 'hasta' no puede ser anterior a la fecha 'desde'");
        }
        int dias = (int) ChronoUnit.DAYS.between(desde, hasta) + 1;
        if (dias > MAXIMO_DIAS) {
            throw new IllegalArgumentException("El periodo del reporte no puede superar " + MAXIMO_DIAS + " días");
        }

        List<Apartamento> apartamentos = apartamentoRepository.listarActivos();
        List<Reserva> reservas = reservaRepository.obtenerTodas();

        // ---------- Ocupación ----------
        List<ReporteResponse.ApartamentoDto> porApartamento = new ArrayList<>();
        int nochesOcupadas = 0;
        for (Apartamento apartamento : apartamentos) {
            int noches = 0;
            for (Reserva reserva : reservas) {
                if (reserva.getApartamento().equals(apartamento.getIdentificacion()) && ocupa(reserva.getEstado())) {
                    noches += nochesDentro(reserva, desde, hasta);
                }
            }
            nochesOcupadas += noches;
            porApartamento.add(new ReporteResponse.ApartamentoDto(
                    apartamento.getIdentificacion().valor(),
                    apartamento.getNombre(),
                    noches,
                    porcentaje(noches, dias)));
        }
        int nochesDisponibles = dias * apartamentos.size();

        // ---------- Reservas con entrada en el periodo ----------
        int[] porEstado = new int[EstadoReserva.values().length];
        int[] porCanal = new int[CanalOrigen.values().length];
        int totalReservas = 0;
        Dinero valorReservado = Dinero.CERO;
        for (Reserva reserva : reservas) {
            LocalDate entrada = reserva.getEstancia().fechaEntrada();
            if (entrada.isBefore(desde) || entrada.isAfter(hasta)) {
                continue;
            }
            totalReservas++;
            porEstado[reserva.getEstado().ordinal()]++;
            porCanal[reserva.getCanalOrigen().ordinal()]++;
            if (reserva.getEstado() != EstadoReserva.CANCELADA && reserva.getEstado() != EstadoReserva.NO_SHOW) {
                valorReservado = valorReservado.mas(reserva.getValor().total());
            }
        }
        List<ReporteResponse.ConteoDto> estados = new ArrayList<>();
        for (EstadoReserva estado : EstadoReserva.values()) {
            estados.add(new ReporteResponse.ConteoDto(estado.name(), porEstado[estado.ordinal()]));
        }
        List<ReporteResponse.ConteoDto> canales = new ArrayList<>();
        for (CanalOrigen canal : CanalOrigen.values()) {
            canales.add(new ReporteResponse.ConteoDto(canal.name(), porCanal[canal.ordinal()]));
        }

        // ---------- Ingresos: pagos recibidos en el periodo ----------
        Dinero ingresos = Dinero.CERO;
        for (Reserva reserva : reservas) {
            Folio folio = folioRepository.buscarPorReserva(reserva.getCodigo()).orElse(null);
            if (folio == null) {
                continue;
            }
            for (Pago pago : folio.getPagos()) {
                LocalDate fecha = pago.getHoraPago().toLocalDate();
                if (!fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
                    ingresos = ingresos.mas(pago.getMonto());
                }
            }
        }

        return new ReporteResponse(
                desde,
                hasta,
                nochesDisponibles,
                nochesOcupadas,
                porcentaje(nochesOcupadas, nochesDisponibles),
                ingresos.valor().longValue(),
                valorReservado.valor().longValue(),
                totalReservas,
                estados,
                canales,
                porApartamento);
    }

    /** Solo ocupan noches las reservas que ya están firmes: una pendiente puede vencerse. */
    private static boolean ocupa(EstadoReserva estado) {
        return estado == EstadoReserva.CONFIRMADA
                || estado == EstadoReserva.EN_CURSO
                || estado == EstadoReserva.FINALIZADA;
    }

    /** Noches de la estancia que caen dentro del periodo (la noche de salida no se ocupa). */
    private static int nochesDentro(Reserva reserva, LocalDate desde, LocalDate hasta) {
        int noches = 0;
        for (LocalDate noche : reserva.getEstancia().nochesOcupadas()) {
            if (!noche.isBefore(desde) && !noche.isAfter(hasta)) {
                noches++;
            }
        }
        return noches;
    }

    private static int porcentaje(int parte, int total) {
        return total <= 0 ? 0 : (int) Math.round(parte * 100.0 / total);
    }
}
