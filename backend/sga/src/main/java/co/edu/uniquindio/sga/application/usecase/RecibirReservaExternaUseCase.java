package co.edu.uniquindio.sga.application.usecase;

import java.util.UUID;
import co.edu.uniquindio.sga.domain.repository.ConflictoCanalRepository;
import co.edu.uniquindio.sga.domain.entity.ConflictoCanal;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.application.exception.ConflictoCanalException;
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
    private final ConflictoCanalRepository conflictoRepository;

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
            CotizadorEstanciaService cotizadorService,
            ConflictoCanalRepository conflictoRepository) {
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
        this.conflictoRepository = conflictoRepository;
    }

    /**
     * Procesa una solicitud de reserva enviada por un canal externo.
     * Garantiza idempotencia (RN-19) y manejo riguroso de conflictos (RN-18).
     */
    @Transactional(noRollbackFor = ConflictoCanalException.class)   // el conflicto registrado se conserva
    public Reserva ejecutar(
            String identificadorExterno,
            Canal canal,
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

        // 1. Validar Idempotencia (RN-19)
        Optional<Reserva> existente = reservaRepository.buscarPorCodigoExterno(identificadorExterno);
        if (existente.isPresent()) {
            return existente.get(); 
        }

        // 2. Obtener la configuración única del alojamiento y el apartamento
        Alojamiento alojamiento = alojamientoRepository.obtenerUnico()
                .orElseThrow(() -> new AlojamientoNoEncontradoException());

        Apartamento apartamento = apartamentoRepository.buscarPorId(idApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(idApartamento));

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
            // Capacidad, mascotas, apartamento inactivo o estancia mínima: se rechaza sin más (409).
            // Si lo que falla es la ocupación (choca con una reserva, un bloqueo o el tiempo de
            // preparación), es un CONFLICTO DE CANAL: se rechaza y se registra (RN-18).
            boolean esChoque = apartamento.estaActivo()
                    && apartamento.admite(totalOcupantes)
                    && apartamento.admiteMascotas(cantidadMascotas.cantidad())
                    && !e.getMessage().toLowerCase().contains("estancia m");
            if (!esChoque) {
                throw e;
            }
            throw registrarConflicto(identificadorExterno, canal, idApartamento, fechaEntrada, fechaSalida,
                    titular, e.getMessage(), ahora);
        }

        // 5. Cotizar y congelar valores y políticas
        List<Temporada> temporadasCotizacion = temporadaRepository.buscarEntre(
                estancia.fechaEntrada(), estancia.fechaSalida().minusDays(1));

        ValorCongelado valorCongelado = cotizadorService.cotizar(
                idApartamento, estancia, ocupantes,
                alojamiento.getUmbralEdadFacturable(), temporadasCotizacion,
                tarifaRepository.buscarPorApartamento(idApartamento)
        );

        // 6. Generar código interno y crear la reserva externa con su código de Booking
        CodigoReserva codigoReserva = generadorCodigo.siguiente(ahora.getYear());

        Reserva reservaExterna = Reserva.crearExterna(
                codigoReserva,
                identificadorExterno,
                idApartamento,
                estancia,
                titular,
                ocupantes,
                canal.getTipoCanal(),
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

    /** Un mensaje repetido que ya causó un conflicto pendiente no crea otro registro. */
    private ConflictoCanalException registrarConflicto(String codigoExterno, Canal canal,
            IdentificacionApartamento apartamento, LocalDate entrada, LocalDate salida, Ocupante titular,
            String motivo, LocalDateTime ahora) {
        ConflictoCanal conflicto = conflictoRepository.buscarPendientePorCodigoExterno(codigoExterno)
                .orElseGet(() -> {
                    ConflictoCanal nuevo = ConflictoCanal.registrar(
                            "CONF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                            canal.getNombre(), codigoExterno, apartamento.valor(), entrada, salida,
                            titular == null ? null : titular.getNombre(),
                            titular == null ? null : titular.getDocumento().numero(),
                            motivo, ahora);
                    conflictoRepository.guardar(nuevo);
                    return nuevo;
                });
        return new ConflictoCanalException(conflicto.getId(), conflicto.getMotivo());
    }
}
