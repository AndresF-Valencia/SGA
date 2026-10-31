package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.DisponibilidadApartamentoService;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class ConsultarDisponibilidadUseCase {

    private final ApartamentoRepository apartamentoRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final ReservaRepository reservaRepository;
    private final BloqueoRepository bloqueoRepository;
    private final DisponibilidadApartamentoService disponibilidadService;
    private final TemporadaRepository temporadaRepository;
    private final TarifaRepository tarifaRepository;
    private final CotizadorEstanciaService cotizador;

    /**
     * Filtros de la búsqueda pública (7.5). Los opcionales van en null; las características
     * deben estar todas presentes en el apartamento.
     */
    public record Criterios(
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            int personas,
            CantidadMascota mascotas,
            Integer capacidadMinima,
            Long precioMin,
            Long precioMax,
            List<String> caracteristicas) {}

    /** Apartamento disponible con su tarifa estimada por noche (null si no tiene tarifa para esas fechas). */
    public record Resultado(Apartamento apartamento, Long tarifaPorNoche) {}

    public ConsultarDisponibilidadUseCase(
            ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository,
            ReservaRepository reservaRepository,
            BloqueoRepository bloqueoRepository,
            DisponibilidadApartamentoService disponibilidadService,
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            CotizadorEstanciaService cotizador) {
        this.apartamentoRepository = apartamentoRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.reservaRepository = reservaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.disponibilidadService = disponibilidadService;
        this.temporadaRepository = temporadaRepository;
        this.tarifaRepository = tarifaRepository;
        this.cotizador = cotizador;
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

    /**
     * Disponibles del rango que cumplen los filtros, con la tarifa estimada por noche:
     * el valor de toda la estancia para el grupo, dividido entre las noches.
     * Un filtro de precio deja fuera a los apartamentos sin tarifa.
     */
    public List<Resultado> buscar(Criterios c) {
        // IllegalArgumentException llega al cliente como 400: son parámetros mal pedidos, no una regla de negocio
        if (c.personas() < 1) {
            throw new IllegalArgumentException("El número de personas debe ser al menos 1");
        }
        if (c.precioMin() != null && c.precioMax() != null && c.precioMin() > c.precioMax()) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el precio máximo");
        }
        Alojamiento alojamiento = obtenerAlojamientoUnico();
        Estancia estancia = new Estancia(c.fechaEntrada(), c.fechaSalida());
        List<String> requeridas = c.caracteristicas() == null ? List.of()
                : c.caracteristicas().stream().map(String::toLowerCase).toList();

        // Los filtros van de barato a caro: primero lo ya disponible (fechas, aforo, mascotas), luego
        // capacidad y características, que solo miran al apartamento, y al final el precio, que exige
        // cotizar. Así solo se cotizan los apartamentos que ya pasaron todo lo demás.
        return listarDisponibles(c.fechaEntrada(), c.fechaSalida(), c.personas(), c.mascotas()).stream()
                .filter(ap -> c.capacidadMinima() == null || ap.getCapacidad().cantidad() >= c.capacidadMinima())
                .filter(ap -> ap.getCaracteristicas().stream().map(String::toLowerCase).toList()
                        .containsAll(requeridas))
                .map(ap -> new Resultado(ap, tarifaPorNoche(ap, estancia, c.personas(), alojamiento)))
                .filter(r -> cumplePrecio(r.tarifaPorNoche(), c.precioMin(), c.precioMax()))
                .toList();
    }

    private static boolean cumplePrecio(Long tarifa, Long min, Long max) {
        if (min == null && max == null) {
            return true;
        }
        return tarifa != null && (min == null || tarifa >= min) && (max == null || tarifa <= max);
    }

    /** Cotiza como si todos los ocupantes fueran adultos facturables; null si faltan temporada o tarifa. */
    private Long tarifaPorNoche(Apartamento ap, Estancia estancia, int personas, Alojamiento alojamiento) {
        LocalDate hoy = estancia.fechaEntrada();
        List<Ocupante> grupo = IntStream.range(0, personas)
                .mapToObj(i -> new Ocupante(new DocumentoIdentidad("ESTIMADO-" + i), "Huésped " + i,
                        hoy.minusYears(30), hoy))
                .toList();
        try {
            ValorCongelado valor = cotizador.cotizar(ap.getIdentificacion(), estancia, grupo,
                    alojamiento.getUmbralEdadFacturable(), temporadaRepository.listar(),
                    tarifaRepository.buscarPorApartamento(ap.getIdentificacion()));
            return valor.total().valor()
                    .divide(BigDecimal.valueOf(valor.noches()), 0, RoundingMode.HALF_UP).longValue();
        } catch (ReglaDominioException e) {
            return null;
        }
    }
}
