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
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Caso de uso (Guía 06): crear una reserva.
 * Solo coordina: busca en los repositorios, deja que los servicios de dominio
 * validen (7.5) y calculen el valor, crea la reserva y abre su folio (F-08).
 */
 
public class CrearReservaUseCase {

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

        public CrearReservaUseCase(ReservaRepository reservaRepository, ApartamentoRepository apartamentoRepository,
                        AlojamientoRepository alojamientoRepository, TemporadaRepository temporadaRepository,
                        TarifaRepository tarifaRepository, BloqueoRepository bloqueoRepository,
                        FolioRepository folioRepository, GeneradorCodigoReserva generadorCodigo,
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
         * @param horaEstimadaLlegada puede ser null: es requisito para confirmar
         *                            (RN-09), no para crear
         */
        @Transactional
        public Reserva ejecutar(IdentificacionAlojamiento alojamientoId, IdentificacionApartamento apartamentoId,
                        Estancia estancia, LocalTime horaEstimadaLlegada,
                        Ocupante titular, List<Ocupante> ocupantes,
                        CanalOrigen canalOrigen, CantidadMascota mascotas, LocalDateTime ahora) {

                Apartamento apartamento = apartamentoRepository.buscarPorId(apartamentoId)
                                .orElseThrow(() -> new ApartamentoNoEncontradoException(apartamentoId));
                Alojamiento alojamiento = alojamientoRepository.buscarPorId(alojamientoId)
                                .orElseThrow(() -> new AlojamientoNoEncontradoException(alojamientoId));

                // 7.5: activo, capacidad (RN-02), mascotas (RNP-02), solapamiento (RN-01),
                // bloqueos (RN-07) y tiempo de preparación (RN-20). Lanza si algo falla.
                disponibilidadService.verificarDisponibilidad(apartamento, estancia, ocupantes.size(), mascotas, null,
                                reservaRepository.buscarActivasPorApartamento(apartamentoId),
                                bloqueoRepository.buscarVigentesPorApartamento(apartamentoId), alojamiento);

                // Temporadas que tocan las noches de la estancia (la de salida no se ocupa)
                List<Temporada> temporadas = temporadaRepository.buscarEntre(
                                estancia.fechaEntrada(), estancia.fechaSalida().minusDays(1));

                // RNP-01: estancia mínima en temporada alta
                estanciaMinimaService.verificarEstanciaMinima(estancia, temporadas);

                // RN-05 y RN-06: valor noche por noche, que queda congelado (RN-22)
                ValorCongelado valor = cotizadorService.cotizar(apartamentoId, estancia, ocupantes,
                                alojamiento.getUmbralEdadFacturable(), temporadas,
                                tarifaRepository.buscarPorApartamento(apartamentoId));

                // RN-03, RN-04 y el titular como ocupante se validan dentro de Reserva.crear
                CodigoReserva codigo = generadorCodigo.siguiente(ahora.getYear());
                Reserva reserva = Reserva.crear(codigo, apartamentoId, estancia, titular, ocupantes, canalOrigen,
                                valor, alojamiento.getVersionPoliticaVigente(), ahora, mascotas);
                if (horaEstimadaLlegada != null) {
                        reserva.indicarHoraEstimadaLlegada(horaEstimadaLlegada, titular.getNombre());
                }
                reservaRepository.guardar(reserva);

                // F-08 y 7.7: el folio se abre con el cargo de alojamiento
                folioRepository.guardar(new Folio(codigo,
                                new Cargo(valor.total(), "Alojamiento", TipoCargo.ALOJAMIENTO, ahora)));

                return reserva;
        }
}