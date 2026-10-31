package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.EstanciaMinimaTemporadaAltaService;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de uso: Modificar una reserva existente (fechas, apartamento, ocupantes o mascotas).
 */
 
public class ModificarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final FolioRepository folioRepository;
    private final ApartamentoRepository apartamentoRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final TemporadaRepository temporadaRepository;
    private final TarifaRepository tarifaRepository;
    private final BloqueoRepository bloqueoRepository;
    private final DisponibilidadApartamentoService disponibilidadService;
    private final EstanciaMinimaTemporadaAltaService estanciaMinimaService;
    private final CotizadorEstanciaService cotizadorService;

    public ModificarReservaUseCase(
            ReservaRepository reservaRepository,
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            BloqueoRepository bloqueoRepository,
            DisponibilidadApartamentoService disponibilidadService,
            EstanciaMinimaTemporadaAltaService estanciaMinimaService,
            CotizadorEstanciaService cotizadorService,
            FolioRepository folioRepository) {
        this.reservaRepository = reservaRepository;
        this.folioRepository = folioRepository;
        this.apartamentoRepository = apartamentoRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.temporadaRepository = temporadaRepository;
        this.tarifaRepository = tarifaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.disponibilidadService = disponibilidadService;
        this.estanciaMinimaService = estanciaMinimaService;
        this.cotizadorService = cotizadorService;
    }

    /**
     * Modifica la estancia de una reserva existente, validando disponibilidad,
     * recotizando y aplicando las reglas del dominio.
     */
        @Transactional
        public Reserva ejecutar(
            CodigoReserva codigoReserva,
            IdentificacionAlojamiento alojamientoId,
            IdentificacionApartamento nuevoApartamentoId,
            Estancia nuevaEstancia,
            List<Ocupante> nuevosOcupantes,
            CantidadMascota nuevasMascotas,
            String autor,
            LocalDateTime ahora) {

        // 1. Buscar la reserva existente
        Reserva reserva = reservaRepository.buscarPorCodigo(codigoReserva)
                .orElseThrow(() -> new ReservaNoEncontradaException(codigoReserva));
        Folio folio = folioRepository.buscarPorReserva(codigoReserva)
                .orElseThrow(() -> new FolioNoEncontradoException(codigoReserva));

        // 2. Buscar entidades relacionadas
        Apartamento apartamento = apartamentoRepository.buscarPorId(nuevoApartamentoId)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(nuevoApartamentoId));
        Alojamiento alojamiento = alojamientoRepository.buscarPorId(alojamientoId)
                .orElseThrow(() -> new AlojamientoNoEncontradoException(alojamientoId));

        // Si no se mandan ocupantes nuevos, se valida con los que ya tiene la reserva
        List<Ocupante> ocupantes = nuevosOcupantes != null && !nuevosOcupantes.isEmpty()
                ? nuevosOcupantes : reserva.getOcupantes();
        int totalOcupantes = ocupantes.size();

        // 3. Validar disponibilidad (RN-01, RN-07, etc.) excluyendo esta misma reserva
        disponibilidadService.verificarDisponibilidad(
                apartamento,
                nuevaEstancia,
                totalOcupantes,
                nuevasMascotas,
                reserva.getCodigo(),
                reservaRepository.buscarActivasPorApartamento(nuevoApartamentoId),
                bloqueoRepository.buscarVigentesPorApartamento(nuevoApartamentoId),
                alojamiento
        );

        // 4. Validar estancia mínima en temporada alta (RNP-01)
        List<Temporada> temporadas = temporadaRepository.buscarEntre(
                nuevaEstancia.fechaEntrada(), nuevaEstancia.fechaSalida().minusDays(1));
        estanciaMinimaService.verificarEstanciaMinima(nuevaEstancia, temporadas);

        // 5. Recotizar los valores con la nueva configuración
        ValorCongelado nuevoValor = cotizadorService.cotizar(
                nuevoApartamentoId,
                nuevaEstancia,
                ocupantes,
                alojamiento.getUmbralEdadFacturable(),
                temporadas,
                tarifaRepository.buscarPorApartamento(nuevoApartamentoId)
        );

        // 6. Delegar la modificación a la entidad de dominio (RN-14, RN-22)
        Dinero ajuste = reserva.modificar(nuevoApartamentoId, nuevaEstancia, ocupantes, nuevoValor, autor, ahora);

        // 7. 7.5: la diferencia de valor va al folio como AJUSTE (positivo o negativo)
        if (!ajuste.esCero()) {
            folio.registrarCargo(new Cargo(ajuste, "Ajuste por modificación de la reserva", TipoCargo.AJUSTE, ahora));
        }

        // 8. Persistir
        reservaRepository.guardar(reserva);
        folioRepository.guardar(folio);

        return reserva;
    }
}