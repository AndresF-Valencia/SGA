package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Lista las reservas de la más reciente a la más antigua (7.5). Todos los filtros son opcionales.
 * "Mis reservas" del huésped es este mismo listado con el titular fijado por quien llama.
 * Devuelve la lista completa; la paginación la arma el controller.
 */
public class ConsultarReservasUseCase {

    public record Filtros(
            EstadoReserva estado,
            IdentificacionApartamento apartamento,
            CanalOrigen canalOrigen,
            LocalDate fechaEntradaDesde,
            LocalDate fechaEntradaHasta,
            DocumentoIdentidad titular) {}

    private final ReservaRepository reservaRepository;

    public ConsultarReservasUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public List<Reserva> ejecutar(Filtros f) {
        return reservaRepository.obtenerTodas().stream()
                .filter(r -> f.estado() == null || r.getEstado() == f.estado())
                .filter(r -> f.apartamento() == null || r.getApartamento().equals(f.apartamento()))
                .filter(r -> f.canalOrigen() == null || r.getCanalOrigen() == f.canalOrigen())
                .filter(r -> f.fechaEntradaDesde() == null
                        || !r.getEstancia().fechaEntrada().isBefore(f.fechaEntradaDesde()))
                .filter(r -> f.fechaEntradaHasta() == null
                        || !r.getEstancia().fechaEntrada().isAfter(f.fechaEntradaHasta()))
                .filter(r -> f.titular() == null || r.getTitular().getDocumento().equals(f.titular()))
                .sorted(Comparator.comparing(Reserva::getCreadaEn).reversed())
                .toList();
    }
}
