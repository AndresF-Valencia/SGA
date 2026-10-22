package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Tarifa;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.TipoCargo;
import co.edu.uniquindio.sga.domain.valueobject.ValorCongelado;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de Dominio: ModificacionReservaService
 * RN-14, RN-22: cambiar las fechas de una reserva.
 *
 * RN-14 pide reevaluar todas las condiciones de 7.5, así que reutiliza los
 * otros servicios en lugar de repetir las reglas:
 * 1. disponibilidad, capacidad, mascotas, bloqueos y tiempo de preparación,
 *    sin que la reserva choque consigo misma;
 * 2. estancia mínima en temporada alta (RP-01);
 * 3. nuevo valor noche por noche (RN-05).
 * Después la reserva cambia sus fechas y devuelve el ajuste, que se registra
 * en el folio como un cargo de tipo AJUSTE. La política congelada no cambia
 * (RN-22).
 */
public class ModificacionReservaService {

    private final DisponibilidadApartamentoService disponibilidad;
    private final EstanciaMinimaTemporadaAltaService estanciaMinima;
    private final CotizadorEstanciaService cotizador;

    public ModificacionReservaService(DisponibilidadApartamentoService disponibilidad,
                                      EstanciaMinimaTemporadaAltaService estanciaMinima,
                                      CotizadorEstanciaService cotizador) {
        if (disponibilidad == null || estanciaMinima == null || cotizador == null) {
            throw new ReglaDominioException("El servicio de modificación necesita sus servicios de apoyo");
        }
        this.disponibilidad = disponibilidad;
        this.estanciaMinima = estanciaMinima;
        this.cotizador = cotizador;
    }

    /**
     * @param folio puede ser null si la reserva aún no tiene folio
     * @return el ajuste: positivo si la reserva sube de valor, negativo si baja
     */
    public Dinero modificarEstancia(Reserva reserva, Apartamento apartamento, Folio folio,
                                    Estancia nuevaEstancia, Alojamiento alojamiento,
                                    List<Reserva> reservasDelApartamento, List<Bloqueo> bloqueosDelApartamento,
                                    List<Temporada> temporadas, List<Tarifa> tarifas,
                                    String autor, LocalDateTime ahora) {
        if (reserva == null || apartamento == null || nuevaEstancia == null || alojamiento == null) {
            throw new ReglaDominioException(
                    "La reserva, el apartamento, la nueva estancia y el alojamiento son obligatorios");
        }
        if (!reserva.getApartamento().equals(apartamento.getIdentificacion())) {
            throw new ReglaDominioException("La reserva no corresponde a este apartamento");
        }
        if (folio != null && !folio.getReserva().equals(reserva.getCodigo())) {
            throw new ReglaDominioException("El folio no corresponde a la reserva");
        }

        // 1. Disponibilidad, sin contar la propia reserva (RN-01, RN-02, RN-07, RN-20)
        disponibilidad.verificarDisponibilidad(apartamento, nuevaEstancia, reserva.totalOcupantes(),
                reserva.getCantidadMascotas(), reserva.getCodigo(),
                reservasDelApartamento, bloqueosDelApartamento, alojamiento);

        // 2. Estancia mínima en temporada alta (RP-01)
        estanciaMinima.verificarEstanciaMinima(nuevaEstancia, temporadas);

        // 3. Nuevo valor con los mismos ocupantes (RN-05, RN-06)
        ValorCongelado nuevoValor = cotizador.cotizar(apartamento.getIdentificacion(), nuevaEstancia,
                reserva.getOcupantes(), alojamiento.getUmbralEdadFacturable(), temporadas, tarifas);

        // 4. La reserva cambia sus fechas y calcula el ajuste (RN-14, RN-04)
        Dinero ajuste = reserva.modificarEstancia(nuevaEstancia, nuevoValor, autor, ahora);

        // 5. El ajuste queda en el folio como un movimiento nuevo (RN-16)
        if (folio != null && !ajuste.esCero()) {
            folio.registrarCargo(new Cargo(ajuste,
                    "Ajuste por modificación de la estancia: " + nuevaEstancia.fechaEntrada()
                            + " a " + nuevaEstancia.fechaSalida(),
                    TipoCargo.AJUSTE, ahora));
        }
        return ajuste;
    }
}
