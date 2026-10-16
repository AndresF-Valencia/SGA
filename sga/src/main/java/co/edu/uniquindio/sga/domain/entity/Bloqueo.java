package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.EstadoBloqueo;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import java.time.LocalDate;

/**
 * Raíz del agregado Bloqueo: decisión administrativa que impide vender un
 * apartamento en un periodo, con su motivo (3.3).
 *
 * Invariantes que garantiza:
 * - todo bloqueo nace VIGENTE;
 * - siempre tiene identificador, apartamento, periodo y motivo;
 * - el apartamento, el periodo y el motivo no cambian después de creado;
 * - no se borra: se levanta, y un bloqueo levantado no impide vender (12.4);
 * - el periodo incluye el último día, a diferencia de la estancia.
 *
 * No garantiza (lo verifica un servicio de dominio):
 * - que no existan reservas activas en el periodo (7.3), porque no conoce las
 * reservas.
 */
public class Bloqueo {

    private final IdentificacionBloqueo identificacionBloqueo;
    private final IdentificacionApartamento apartamento;
    private final Periodo periodo;
    private final MotivoBloqueo motivo;
    private EstadoBloqueo estado;

    public Bloqueo(IdentificacionBloqueo id, IdentificacionApartamento apartamento, Periodo periodo, MotivoBloqueo motivo) {
        if (id == null) {
            throw new ReglaDominioException("El bloqueo debe tener un identificador");
        }
        if (apartamento == null) {
            throw new ReglaDominioException("El bloqueo debe estar asociado a un apartamento");
        }
        if (periodo == null) {
            throw new ReglaDominioException("El bloqueo debe tener un periodo definido");
        }
        if (motivo == null) {
            throw new ReglaDominioException("El bloqueo debe tener un motivo");
        }
        this.identificacionBloqueo = id;
        this.apartamento = apartamento;
        this.periodo = periodo;
        this.motivo = motivo;
        this.estado = EstadoBloqueo.VIGENTE;
    }

    /** RN-07: un bloqueo vigente impide vender las noches de su periodo. */
    public boolean bloquea(LocalDate noche) {
        return estado.impideVenta() && periodo.incluye(noche);
    }

    /**
     * RN-07: una estancia se ve afectada si alguna de sus noches está bloqueada.
     */
    public boolean afecta(Estancia estancia) {
        for (LocalDate noche : estancia.nochesOcupadas()) {
            if (bloquea(noche)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Eliminación lógica (12.4): el bloqueo no se borra, deja de impedir la venta.
     */
    public void levantar() {
        if (estado == EstadoBloqueo.LEVANTADO) {
            throw new ReglaDominioException("El bloqueo ya está levantado");
        }
        estado = EstadoBloqueo.LEVANTADO;
    }

    public IdentificacionBloqueo getIdentificacionBloqueo() {
        return identificacionBloqueo;
    }

    public IdentificacionApartamento getApartamento() {
        return apartamento;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public MotivoBloqueo getMotivo() {
        return motivo;
    }

    public EstadoBloqueo getEstado() {
        return estado;
    }

    // Dos bloqueos son el mismo si tienen el mismo identificador.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bloqueo otro)) {
            return false;
        }
        return this.identificacionBloqueo.equals(otro.identificacionBloqueo);
    }

    @Override
    public int hashCode() {
        return identificacionBloqueo.hashCode();
    }
}
