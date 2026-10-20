package co.edu.uniquindio.sga.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.service.LiquidacionEstanciaService;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

@Service
public class CrearReservaUseCase {

        private final ReservaRepository reservaRepository;
        private final ApartamentoRepository apartamentoRepository;
        private final AlojamientoRepository alojamientoRepository;
        private final TemporadaRepository temporadaRepository;
        private final PoliticaCancelacion politicaCancelacion;
        private final FolioRepository folioRepository;
        private final DisponibilidadApartamentoService disponibilidadApartamentoService;
        private final LiquidacionEstanciaService liquidacionService;
        private final TiempoPreparacion tiempoPreparacion;

        public CrearReservaUseCase(ReservaRepository reservaRepository, AlojamientoRepository alojamientoRepository,
                        ApartamentoRepository apartamentoRepository,
                        TemporadaRepository temporadaRepository, PoliticaCancelacion politicaCancelacion,
                        DisponibilidadApartamentoService disponibilidadApartamentoService,
                        LiquidacionEstanciaService liquidacionService,
                        TiempoPreparacion tiempoPreparacion,
                        FolioRepository folioRepository) {
                this.reservaRepository = reservaRepository;
                this.apartamentoRepository = apartamentoRepository;
                this.alojamientoRepository = alojamientoRepository;
                this.temporadaRepository = temporadaRepository;
                this.politicaCancelacion = politicaCancelacion;
                this.disponibilidadApartamentoService = disponibilidadApartamentoService;
                this.liquidacionService = liquidacionService;
                this.tiempoPreparacion = tiempoPreparacion;
                this.folioRepository = folioRepository;
        }

        @Transactional
        public Reserva ejecutar(IdentificacionAlojamiento alojamientoId, IdentificacionApartamento apartamentoId,
                        Estancia estancia,
                        HoraEstimadaLlegada horaEstimadaLlegada,
                        Ocupante titular, List<Ocupante> ocupantes,
                        CanalOrigen canalOrigen, LocalDate fechaActual, int totalOcupantes, CantidadMascota mascotas,
                        List<Reserva> reservas, List<Bloqueo> bloqueosExistentes, CodigoReserva codigo) {

                Apartamento apartamento = apartamentoRepository.buscarPorId(apartamentoId)
                                .orElseThrow(() -> new ApartamentoNoEncontradoException(apartamentoId));

                Alojamiento alojamiento = alojamientoRepository.buscarPorId(alojamientoId)
                                .orElseThrow(() -> new AlojamientoNoEncontradoException(alojamientoId));

                // 7.5 condición 3
                apartamento.estaActivo();

                // RN-01, RN-07, RN-20
                disponibilidadApartamentoService.estaDisponible(apartamento, estancia, totalOcupantes, mascotas, null,
                                bloqueosExistentes, alojamiento);

                Dinero valor = liquidacionService
                                .liquidar(apartamento, estancia, ocupantes, temporadaRepository.buscarTodas())
                                .total();

                // RN-02, RN-04 y RN-22 se verifican dentro de Reserva.crear(...)
                Reserva reserva = Reserva.crear(codigo, apartamentoId, estancia,
                                titular, ocupantes, canalOrigen, valor, politicaCancelacion, fechaActual, mascotas);

                if (horaEstimadaLlegada != null) {
                        reserva.indicarHoraEstimadaLlegada(horaEstimadaLlegada.horaEstimada(), titular.getNombre());
                }

                reservaRepository.guardar(reserva);

                // F-08: el folio se abre al crear la reserva
                folioRepository.guardar(Folio.abrir(codigo, valor, fechaActual));

                return reserva;

        }
}
