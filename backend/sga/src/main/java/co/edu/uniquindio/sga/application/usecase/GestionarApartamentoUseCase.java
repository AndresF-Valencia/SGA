package co.edu.uniquindio.sga.application.usecase;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ApartamentoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

/**
 * Caso de uso: CRUD de apartamentos (7.3).
 *
 * - La eliminación es lógica: "eliminar" desactiva, y solo se permite si el
 *   apartamento no tiene reservas activas.
 * - Un apartamento solo se activa si tiene tarifa en todas las temporadas.
 *   Por eso uno nuevo se crea inactivo hasta que se configuren sus tarifas.
 */
public class GestionarApartamentoUseCase {

    private final ApartamentoRepository apartamentoRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final ReservaRepository reservaRepository;
    private final TarifaRepository tarifaRepository;
    private final TemporadaRepository temporadaRepository;

    public GestionarApartamentoUseCase(ApartamentoRepository apartamentoRepository,
            AlojamientoRepository alojamientoRepository, ReservaRepository reservaRepository,
            TarifaRepository tarifaRepository, TemporadaRepository temporadaRepository) {
        this.apartamentoRepository = apartamentoRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.reservaRepository = reservaRepository;
        this.tarifaRepository = tarifaRepository;
        this.temporadaRepository = temporadaRepository;
    }

    /** Crea el apartamento inactivo: se activa cuando tenga tarifas completas. */
    @Transactional
    public Apartamento crear(IdentificacionApartamento id, String nombre, TiempoPreparacion tiempoPreparacion,
            Dormitorio dormitorios, CapacidadOcupantes capacidad, LimiteMascota limiteMascotas,
            List<String> caracteristicas, List<String> imagenes) {
        if (id == null) {
            throw new ReglaDominioException("La identificación del apartamento es obligatoria");
        }
        if (apartamentoRepository.existePorId(id)) {
            throw new ReglaDominioException("Ya existe un apartamento con identificación: " + id.valor());
        }
        var alojamiento = alojamientoRepository.obtenerUnico()
                .orElseThrow(AlojamientoNoEncontradoException::new);

        Apartamento apartamento = new Apartamento(id, nombre, tiempoPreparacion, alojamiento.getIdentificacion(),
                dormitorios, capacidad, limiteMascotas, caracteristicas, imagenes);
        apartamento.desactivar();
        apartamentoRepository.guardar(apartamento);
        return apartamento;
    }

    @Transactional(readOnly = true)
    public Apartamento obtener(IdentificacionApartamento id) {
        return buscar(id);
    }

    @Transactional(readOnly = true)
    public List<Apartamento> listar() {
        return apartamentoRepository.listar();
    }

    @Transactional
    public Apartamento actualizar(IdentificacionApartamento id, String nombre, TiempoPreparacion tiempoPreparacion,
            Dormitorio dormitorios, CapacidadOcupantes capacidad, LimiteMascota limiteMascotas,
            List<String> caracteristicas, List<String> imagenes) {
        Apartamento apartamento = buscar(id);
        apartamento.actualizar(nombre, tiempoPreparacion, dormitorios, capacidad, limiteMascotas,
                caracteristicas, imagenes);
        apartamentoRepository.guardar(apartamento);
        return apartamento;
    }

    /** Eliminación lógica: no se permite con reservas activas (PENDIENTE, CONFIRMADA o EN_CURSO). */
    @Transactional
    public Apartamento desactivar(IdentificacionApartamento id) {
        Apartamento apartamento = buscar(id);
        if (!reservaRepository.buscarActivasPorApartamento(id).isEmpty()) {
            throw new ReglaDominioException(
                    "No se puede deshabilitar el apartamento " + id.valor() + " porque tiene reservas activas");
        }
        apartamento.desactivar();
        apartamentoRepository.guardar(apartamento);
        return apartamento;
    }

    /** Solo se activa si tiene tarifa para cada una de las temporadas registradas. */
    @Transactional
    public Apartamento activar(IdentificacionApartamento id) {
        Apartamento apartamento = buscar(id);
        List<Temporada> temporadas = temporadaRepository.listar();
        if (temporadas.isEmpty()) {
            throw new ReglaDominioException("No se puede activar el apartamento: no hay temporadas registradas");
        }
        for (Temporada temporada : temporadas) {
            if (tarifaRepository.buscarPorApartamentoYTemporada(id, temporada.getIdentificacion()).isEmpty()) {
                throw new ReglaDominioException("No se puede activar el apartamento " + id.valor()
                        + ": falta la tarifa de la temporada '" + temporada.getNombre() + "'");
            }
        }
        apartamento.activar();
        apartamentoRepository.guardar(apartamento);
        return apartamento;
    }

    private Apartamento buscar(IdentificacionApartamento id) {
        if (id == null) {
            throw new ReglaDominioException("La identificación del apartamento es obligatoria");
        }
        return apartamentoRepository.buscarPorId(id).orElseThrow(() -> new ApartamentoNoEncontradoException(id));
    }
}
