package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaAltaService;
import co.edu.uniquindio.sga.domain.valueobject.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecibirReservaExternaUseCase {

    private final ReservaRepository reservaRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final TemporadaRepository temporadaRepository;
    private final TarifaRepository tarifaRepository;
    private final BloqueoRepository bloqueoRepository;
    private final FolioRepository folioRepository;
    private final GeneradorCodigoReserva generadorCodigo;
    private final DisponibilidadApartamentoService disponibilidadService;
    private final EstanciaMinimaTemporadaAltaService estanciaMinimaService;
    private final CotizadorEstanciaService cotizadorService;

    public RecibirReservaExternaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            BloqueoRepository bloqueoRepository,
            FolioRepository folioRepository,
            GeneradorCodigoReserva generadorCodigo,
            DisponibilidadApartamentoService disponibilidadService,
            EstanciaMinimaTemporadaAltaService estanciaMinimaService,
            CotizadorEstanciaService cotizadorService) {
        this.reservaRepository = reservaRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.temporadaRepository = temporadaRepository;
        this.tarifaRepository = tarifaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.folioRepository = folioRepository;
        this.generadorCodigo = generadorCodigo;
        this.disponibilidadService = disponibilidadService;
        this.estanciaMinimaService = estanciaMinimaService;
        this.cotizadorService = cotizadorService;
    }

    /**
     * Procesa una solicitud de reserva enviada por un canal externo.
     * Garantiza idempotencia (RN-19) y manejo riguroso de conflictos (RN-18).
     */
        @Transactional
        public Reserva ejecutar(
            String identificadorExterno,
            CanalOrigen canalOrigen,
            IdentificacionAlojamiento alojamientoId,
            IdentificacionApartamento idApartamento,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            Ocupante titular,
            List<Ocupante> ocupantes,
            CantidadMascota cantidadMascotas,
            LocalDateTime ahora) {

        if (identificadorExterno == null || identificadorExterno.isBlank()) {
            throw new ReglaDominioException("La reserva externa debe indicar su identificador de canal");
        }

        // 1. Validar Idempotencia (RN-19): Si ya existe, se retorna la misma sin duplicar
        Optional<Reserva> existente = reservaRepository.buscarPorCanalYIdentificadorExterno(canalOrigen, identificadorExterno);
        if (existente.isPresent()) {
            return existente.get(); 
        }

        // 2. Buscar entidades con excepciones personalizadas de aplicación
        Apartamento apartamento = apartamentoRepository.buscarPorId(idApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(idApartamento));
        Alojamiento alojamiento = alojamientoRepository.buscarPorId(alojamientoId)
                .orElseThrow(() -> new AlojamientoNoEncontradoException(alojamientoId));

        // 3. Construir la Estancia
        Estancia estancia = new Estancia(fechaEntrada, fechaSalida);
        int totalOcupantes = ocupantes != null ? ocupantes.size() : 0;

        // 4. Manejo de Conflictos (RN-18): Validar disponibilidad y reglas de temporada alta
        try {
            disponibilidadService.verificarDisponibilidad(
                    apartamento,
                    estancia,
                    totalOcupantes,
                    cantidadMascotas,
                    null, // Sin reserva excluida
                    reservaRepository.buscarActivasPorApartamento(idApartamento),
                    bloqueoRepository.buscarVigentesPorApartamento(idApartamento),
                    alojamiento
            );

            // Consultar temporadas para verificar restricciones de estancia mínima
            List<Temporada> temporadas = temporadaRepository.buscarEntre(
                    estancia.fechaEntrada(), estancia.fechaSalida().minusDays(1));

            estanciaMinimaService.verificarEstanciaMinima(estancia, temporadas);

        } catch (ReglaDominioException e) {
            // RN-18: Si colisiona con reservas, bloqueos o reglas, se rechaza limpiamente
            throw new ReglaDominioException("Conflicto de canal: La reserva externa colisiona con una reserva o bloqueo vigente. Rechazada. Motivo: " + e.getMessage());
        }

        // 5. Cotizar y congelar valores y políticas (idéntico a una reserva interna)
        List<Temporada> temporadasCotizacion = temporadaRepository.buscarEntre(
                estancia.fechaEntrada(), estancia.fechaSalida().minusDays(1));

        ValorCongelado valorCongelado = cotizadorService.cotizar(
                idApartamento, estancia, ocupantes,
                alojamiento.getUmbralEdadFacturable(), temporadasCotizacion,
                tarifaRepository.buscarPorApartamento(idApartamento)
        );

        // 6. Generar código y crear la reserva en estado PENDIENTE
        CodigoReserva codigoReserva = generadorCodigo.siguiente(ahora.getYear());

        Reserva reservaExterna = Reserva.crear(
                codigoReserva,
                idApartamento,
                estancia,
                titular,
                ocupantes,
                canalOrigen,
                valorCongelado,
                alojamiento.getVersionPoliticaVigente(),
                ahora,
                cantidadMascotas
        );

        // 7. Persistir la reserva y abrir su respectivo folio (F-08)
        reservaRepository.guardar(reservaExterna);

        folioRepository.guardar(new Folio(codigoReserva,
                new Cargo(valorCongelado.total(), "Alojamiento", TipoCargo.ALOJAMIENTO, ahora)));

        return reservaExterna;
    }
}