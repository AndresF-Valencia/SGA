package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.time.LocalDate;
import java.util.List;

public class ConsultarDisponibilidadUseCase {

    private final ApartamentoRepository apartamentoRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final ReservaRepository reservaRepository;
    private final BloqueoRepository bloqueoRepository;
    private final DisponibilidadApartamentoService disponibilidadService;

    public ConsultarDisponibilidadUseCase(
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            ReservaRepository reservaRepository,
            BloqueoRepository bloqueoRepository,
            DisponibilidadApartamentoService disponibilidadService) {
        this.apartamentoRepository = apartamentoRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.reservaRepository = reservaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.disponibilidadService = disponibilidadService;
    }

    private Alojamiento obtenerAlojamientoUnico() {
        return alojamientoRepository.obtenerUnico()
                .orElseThrow(() -> new IllegalStateException("No se ha configurado el alojamiento en el sistema"));
    }
    /**
     * Verifica si un apartamento específico está disponible para una estancia,
     * número de ocupantes y cantidad de mascotas.
     */
    public boolean ejecutar(
            IdentificacionApartamento idApartamento,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            int totalOcupantes,
            CantidadMascota cantidadMascotas) {

        Estancia estancia = new Estancia(fechaEntrada, fechaSalida);
        Alojamiento alojamiento = obtenerAlojamientoUnico();

        Apartamento apartamento = apartamentoRepository.buscarPorId(idApartamento)
                .orElseThrow(() -> new ApartamentoNoEncontradoException(idApartamento));

        return disponibilidadService.estaDisponible(
                apartamento,
                estancia,
                totalOcupantes,
                cantidadMascotas,
                reservaRepository.buscarActivasPorApartamento(idApartamento),
                bloqueoRepository.buscarVigentesPorApartamento(idApartamento),
                alojamiento
        );
    }

    /**
     * Lista todos los apartamentos disponibles en el alojamiento para un rango de fechas.
     */
    public List<Apartamento> listarDisponibles(
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            int totalOcupantes,
            CantidadMascota cantidadMascotas) {

        Alojamiento alojamiento = obtenerAlojamientoUnico();
        Estancia estancia = new Estancia(fechaEntrada, fechaSalida);
        List<Apartamento> todosLosApartamentos = apartamentoRepository.listar();

        return todosLosApartamentos.stream()
                .filter(ap -> disponibilidadService.estaDisponible(
                        ap,
                        estancia,
                        totalOcupantes,
                        cantidadMascotas,
                        reservaRepository.buscarActivasPorApartamento(ap.getIdentificacion()),
                        bloqueoRepository.buscarVigentesPorApartamento(ap.getIdentificacion()),
                        alojamiento
                ))
                .toList();
    }
}