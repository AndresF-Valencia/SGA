package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;

import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

public class Apartamento {

    private final IdentificacionApartamento identificacion;

    private final IdentificacionAlojamiento indentificacionAlojamiento; // identificacion al alojamiento que pertence

    private String nombre;

    private Dormitorio dormitorios; // encapsulado en VO

    private CapacidadOcupantes capacidad; // encapsulado en VO

    private EstadoOperativo estadoOperativo; // encapsulado en VO

    private LimiteMascota limiteMascotas; // encapsulado en VO

    private TiempoPreparacion tiempoPreparacion; // encapsulado en VO

    private boolean activo;

    public Apartamento(IdentificacionApartamento identificacion, String nombre,
            TiempoPreparacion tiempoPreparacion,

            IdentificacionAlojamiento identificacionAlojamiento, Dormitorio dormitorios, CapacidadOcupantes capacidad,
            LimiteMascota limiteMascotas) {

        if (identificacion == null) {

            throw new ReglaDominioException("El apartamento debe tener identificación");

        }
        if (identificacionAlojamiento == null) {
            throw new ReglaDominioException("El apartamento debe tener una identificación de alojamiento");
        }

        if (nombre == null || nombre.isBlank()) {

            throw new ReglaDominioException("El apartamento debe tener un nombre");

        }

        if (dormitorios.cantidad() < 1) {

            throw new ReglaDominioException("El apartamento debe tener al menos un dormitorio");

        }

        if (capacidad.cantidad() < 1) {

            throw new ReglaDominioException("La capacidad del apartamento debe ser al menos 1");

        }

        if (limiteMascotas.limite() < 0) {

            throw new ReglaDominioException(" El límite de mascotas no puede ser negativo");

        }
        if (tiempoPreparacion == null) {
            throw new ReglaDominioException("El apartamento debe tener un tiempo de preparación");
        }

        this.identificacion = identificacion;

        this.indentificacionAlojamiento = identificacionAlojamiento;

        this.nombre = nombre;

        this.tiempoPreparacion = tiempoPreparacion;

        this.dormitorios = dormitorios;

        this.capacidad = capacidad;

        this.estadoOperativo = EstadoOperativo.PREPARADO;

        this.activo = true;

        this.limiteMascotas = limiteMascotas;

    }

    // Comportamiento: los nombres vienen del lenguaje del dominio

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
     * este metodo se encarga de autorizar la admision de mascotas
     * 
     * @param limiteMascotas limite de mascotas permitido en el apartamento
     * @param cantidad       cantidad de mascotas que se desea alojar en el
     *                       apartamento
     * @return retorna true si adimite la cantidad de mascotas, false si no lo haces
     */
    public boolean adminteMascotas(int cantidad) {

        if (cantidad < 0) {
            throw new ReglaDominioException("La cantidad de mascotas no puede ser negativa");
        }

        return cantidad <= limiteMascotas.limite();
    }

    private void verificarEstado(EstadoOperativo estado) {
        if (!this.estadoOperativo.puedeTransicionarA(estado)) {
            throw new ReglaDominioException(
                    "El apartamento no puede pasar de " + this.estadoOperativo + " a " + estado);
        }
    }

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

    // ------ CONSULTAS ------

    public IdentificacionApartamento getIdentificacion() {

        return identificacion;

    }

    public IdentificacionAlojamiento getIndentificacionAlojamiento() {

        return indentificacionAlojamiento;

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

    public void setLimiteMascotas(LimiteMascota limiteMascotas) {
        this.limiteMascotas = limiteMascotas;
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