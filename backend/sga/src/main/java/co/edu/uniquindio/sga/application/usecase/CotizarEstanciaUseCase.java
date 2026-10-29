package co.edu.uniquindio.sga.application.usecase;

import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.application.exception.AlojamientoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.TarifaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.time.LocalDate;
import java.util.List;

 
public class CotizarEstanciaUseCase {

    private final TemporadaRepository temporadaRepository;
    private final TarifaRepository tarifaRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final CotizadorEstanciaService cotizadorEstanciaService;

    public CotizarEstanciaUseCase(
            TemporadaRepository temporadaRepository,
            TarifaRepository tarifaRepository,
            AlojamientoRepository alojamientoRepository,
            CotizadorEstanciaService cotizadorEstanciaService) {
        this.temporadaRepository = temporadaRepository;
        this.tarifaRepository = tarifaRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.cotizadorEstanciaService = cotizadorEstanciaService;
    }

    /**
     * Ejecuta la cotización de una estancia para un apartamento determinado.
     *
     * @param idAlojamiento   Identificación del alojamiento (para consultar el umbral de edad configurado).
     * @param idApartamento   Identificación del apartamento a cotizar.
     * @param fechaEntrada    Fecha de entrada (check-in).
     * @param fechaSalida     Fecha de salida (check-out).
     * @param ocupantes       Lista completa de ocupantes con sus fechas de nacimiento.
     * @return ValorCongelado con el desglose de tarifas noche por noche.
     */
    @Transactional
    public ValorCongelado ejecutar(
            IdentificacionApartamento idApartamento,
            LocalDate fechaEntrada,
            LocalDate fechaSalida,
            List<Ocupante> ocupantes) {

        // 1. Construir la Estancia (valida automáticamente RN-03)
        Estancia estancia = new Estancia(fechaEntrada, fechaSalida);

        // 2. Obtener el alojamiento único del sistema y su umbral
        Alojamiento alojamiento = alojamientoRepository.obtenerUnico()
                .orElseThrow(() -> new AlojamientoNoEncontradoException());

        UmbralEdadFacturable umbral = alojamiento.getUmbralEdadFacturable();

        // 3. Consultar las temporadas y las tarifas registradas para el apartamento
        List<Temporada> temporadas = temporadaRepository.listar();
        List<Tarifa> tarifas = tarifaRepository.buscarPorApartamento(idApartamento);

        // 4. Delegar el cálculo noche por noche al servicio de dominio
        return cotizadorEstanciaService.cotizar(
                idApartamento,
                estancia,
                ocupantes,
                umbral,
                temporadas,
                tarifas
        );
    }
}