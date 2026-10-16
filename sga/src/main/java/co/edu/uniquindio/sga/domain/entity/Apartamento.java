package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

/**
 * Raíz del agregado Apartamento: unidad vendible del alojamiento.
 *
 * Invariantes que garantiza:
 * - siempre tiene identificación, alojamiento, nombre, dormitorios, capacidad,
 * límite de mascotas y tiempo de preparación;
 * - nace activo y PREPARADO;
 * - el estado operativo solo cambia por las transiciones permitidas (7.6),
 * y un apartamento OCUPADO nunca pasa a FUERA_DE_SERVICIO;
 * - la eliminación es lógica: se desactiva, nunca se borra (7.3).
 */
public class Apartamento {

    private final IdentificacionApartamento identificacion;
    private final IdentificacionAlojamiento identificacionAlojamiento; // alojamiento al que pertenece
    private String nombre;
    private Dormitorio dormitorios;
    private CapacidadOcupantes capacidad;
    private EstadoOperativo estadoOperativo;
    private LimiteMascota limiteMascotas;
    private TiempoPreparacion tiempoPreparacion;
    private boolean activo;

    public Apartamento(IdentificacionApartamento identificacion, String nombre,
            TiempoPreparacion tiempoPreparacion, IdentificacionAlojamiento identificacionAlojamiento,
            Dormitorio dormitorios, CapacidadOcupantes capacidad, LimiteMascota limiteMascotas) {

        if (identificacion == null) {
            throw new ReglaDominioException("El apartamento debe tener identificación");
        }
        if (identificacionAlojamiento == null) {
            throw new ReglaDominioException("El apartamento debe tener una identificación de alojamiento");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El apartamento debe tener un nombre");
        }
        // Los value objects ya validan sus propios valores (al menos 1 dormitorio,
        // etc.);
        // aquí solo falta exigir que lleguen.
        if (dormitorios == null) {
            throw new ReglaDominioException("El apartamento debe indicar sus dormitorios");
        }
        if (capacidad == null) {
            throw new ReglaDominioException("El apartamento debe indicar su capacidad");
        }
        if (limiteMascotas == null) {
            throw new ReglaDominioException("El apartamento debe indicar su límite de mascotas");
        }
        if (tiempoPreparacion == null) {
            throw new ReglaDominioException("El apartamento debe tener un tiempo de preparación");
        }

        this.identificacion = identificacion;
        this.identificacionAlojamiento = identificacionAlojamiento;
        this.nombre = nombre;
        this.tiempoPreparacion = tiempoPreparacion;
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.limiteMascotas = limiteMascotas;
        this.estadoOperativo = EstadoOperativo.PREPARADO;
        this.activo = true;
    }

    // ------ COMPORTAMIENTO ------

    /** RN-02: la capacidad es un tope rígido, sin excepciones. */
    public boolean admite(int totalOcupantes) {
        return totalOcupantes > 0 && totalOcupantes <= capacidad.cantidad();
    }

    /** RN-11: solo se entrega un apartamento activo y PREPARADO. */
    public boolean puedeRecibirGrupo() {
        return activo && estadoOperativo.permiteRegistro();
    }

    /** La eliminación de apartamentos es lógica (7.3). */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * L-16: admite la cantidad de mascotas si no supera el límite del apartamento.
     */
    public boolean admiteMascotas(int cantidad) {
        if (cantidad < 0) {
            throw new ReglaDominioException("La cantidad de mascotas no puede ser negativa");
        }
        return cantidad <= limiteMascotas.limite();
    }

    /** L-16: el administrador cambia el límite de mascotas del apartamento. */
    public void cambiarLimiteMascotas(LimiteMascota nuevoLimite) {
        if (nuevoLimite == null) {
            throw new ReglaDominioException("Debe indicarse el nuevo límite de mascotas");
        }
        this.limiteMascotas = nuevoLimite;
    }

    // ------ ESTADO OPERATIVO (7.6) ------

    /** RN-11: el grupo hace el registro. PREPARADO → OCUPADO. */
    public void marcarOcupado() {
        cambiarEstado(EstadoOperativo.OCUPADO);
    }

    /** Salida del grupo. OCUPADO → PENDIENTE_PREPARACION. */
    public void marcarPendientePreparacion() {
        cambiarEstado(EstadoOperativo.PENDIENTE_PREPARACION);
    }

    /**
     * El personal de servicio empieza a preparar. PENDIENTE_PREPARACION →
     * EN_PREPARACION.
     */
    public void marcarEnPreparacion() {
        cambiarEstado(EstadoOperativo.EN_PREPARACION);
    }

    /** Terminó la preparación. EN_PREPARACION → PREPARADO. */
    public void marcarPreparado() {
        cambiarEstado(EstadoOperativo.PREPARADO);
    }

    /** Decisión administrativa, desde cualquier estado no ocupado (7.6). */
    public void marcarFueraDeServicio() {
        cambiarEstado(EstadoOperativo.FUERA_DE_SERVICIO);
    }

    private void cambiarEstado(EstadoOperativo estado) {
        verificarEstado(estado);
        this.estadoOperativo = estado;
    }

    private void verificarEstado(EstadoOperativo estado) {
        if (!this.estadoOperativo.puedeTransicionarA(estado)) {
            throw new ReglaDominioException(
                    "El apartamento no puede pasar de " + this.estadoOperativo + " a " + estado);
        }
    }

    // ------ CONSULTAS ------

    public IdentificacionApartamento getIdentificacion() {
        return identificacion;
    }

    public IdentificacionAlojamiento getIdentificacionAlojamiento() {
        return identificacionAlojamiento;
    }

    public String getNombre() {
        return nombre;
    }

    public Dormitorio getDormitorios() {
        return dormitorios;
    }

    public CapacidadOcupantes getCapacidad() {
        return capacidad;
    }

    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }

    public boolean estaActivo() {
        return activo;
    }

    public TiempoPreparacion getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public LimiteMascota getLimiteMascotas() {
        return limiteMascotas;
    }

    // Dos apartamentos son el mismo si tienen la misma identificación,
    // aunque hayan cambiado de nombre, de dotación o de estado.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Apartamento otro)) {
            return false;
        }
        return this.identificacion.equals(otro.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}