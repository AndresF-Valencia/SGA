package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CargoNoche;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Dominio: CotizadorEstanciaService
 * RN-05, RN-06, RN-22: Calcula el valor noche por noche cruzando temporadas,
 * tarifas y el umbral de edad de los ocupantes, generando el ValorCongelado.
 */
public class CotizadorEstanciaService {

    /**
     * Cuenta cuántos ocupantes son facturables a la fecha de entrada según el umbral del alojamiento (RN-06).
     */
    public int contarOcupantesFacturables(Estancia estancia, List<Ocupante> ocupantes, UmbralEdadFacturable umbral) {
        if (estancia == null || ocupantes == null || umbral == null) {
            throw new ReglaDominioException("Los datos para contar ocupantes facturables no pueden ser nulos");
        }
        
        int facturables = 0;
        for (Ocupante ocupante : ocupantes) {
            if (ocupante.esFacturableEn(estancia, umbral)) {
                facturables++;
            }
        }
        
        if (facturables == 0) {
            throw new ReglaDominioException("La estancia debe tener al menos un ocupante facturable según el umbral de edad");
        }
        
        return facturables;
    }

    /**
     * Cotiza la estancia completa noche por noche y retorna el ValorCongelado con sus CargoNoche.
     */
    public ValorCongelado cotizar(IdentificacionApartamento apartamentoId, Estancia estancia,
                                  List<Ocupante> ocupantes, UmbralEdadFacturable umbral,
                                  List<Temporada> temporadasDisponibles, List<Tarifa> tarifasDisponibles) {
        if (apartamentoId == null || estancia == null || ocupantes == null) {
            throw new ReglaDominioException("Los parámetros de cotización son obligatorios");
        }

        int ocupantesFacturables = contarOcupantesFacturables(estancia, ocupantes, umbral);
        List<LocalDate> fechas = estancia.nochesOcupadas();
        List<CargoNoche> detalleCargos = new ArrayList<>();

        for (LocalDate fechaNoche : fechas) {
            // 1. Encontrar la temporada a la que pertenece esta noche
            Temporada temporadaAplicable = temporadasDisponibles.stream()
                    .filter(t -> t.cubre(fechaNoche))
                    .findFirst()
                    .orElseThrow(() -> new ReglaDominioException("No se encontró una temporada configurada para la fecha: " + fechaNoche));

            // 2. Encontrar la tarifa para este apartamento en dicha temporada
            Tarifa tarifaAplicable = tarifasDisponibles.stream()
                    .filter(tf -> tf.getApartamento().equals(apartamentoId) && tf.getTemporada().equals(temporadaAplicable.getIdentificacion()))
                    .findFirst()
                    .orElseThrow(() -> new ReglaDominioException("No se encontró tarifa para el apartamento en la temporada: " + temporadaAplicable.getNombre()));

            // 3. Crear el CargoNoche por cada noche (el propio record calcula su subtotal)
            CargoNoche cargoNoche = new CargoNoche(
                    fechaNoche,
                    temporadaAplicable.getNombre(),
                    tarifaAplicable.getValorPorOcupanteNoche(),
                    ocupantesFacturables
            );
            
            detalleCargos.add(cargoNoche);
        }

        return new ValorCongelado(detalleCargos);
    }
}