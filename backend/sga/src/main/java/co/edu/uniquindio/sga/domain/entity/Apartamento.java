package co.edu.uniquindio.sga.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CapacidadOcupantes;
import co.edu.uniquindio.sga.domain.valueobject.Dormitorio;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.LimiteMascota;
import co.edu.uniquindio.sga.domain.valueobject.Novedad;
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
    private final List<Novedad> novedades = new ArrayList<>();
    private final List<String> caracteristicas = new ArrayList<>();
    private final List<String> imagenes = new ArrayList<>();
    private String nombre;
    private Dormitorio dormitorios;
    private CapacidadOcupantes capacidad;
    private EstadoOperativo estadoOperativo;
    private LimiteMascota limiteMascotas;
    private TiempoPreparacion tiempoPreparacion;
    private boolean activo;

    public Apartamento(IdentificacionApartamento identificacion, String nombre,
            TiempoPreparacion tiempoPreparacion, IdentificacionAlojamiento identificacionAlojamiento,
            Dormitorio dormitorios, CapacidadOcupantes capacidad, LimiteMascota limiteMascotas,List<String> caracteristicas,List<String> imagenes) {

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

        if (caracteristicas == null) {
            throw new ReglaDominioException("La lista de características del apartamento no puede ser nula");
        }

        if (imagenes == null) {
            throw new ReglaDominioException("La lista de imágenes del apartamento no puede ser nula");
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
        this.caracteristicas.addAll(caracteristicas);
        this.imagenes.addAll(imagenes);
    }

    /**
     * Vuelve a armar un apartamento que ya estaba guardado (la usa la persistencia).
     * El estado operativo se asigna tal cual, sin pasar por la máquina de estados,
     * porque ya fue validado cuando cambió.
     */
    public static Apartamento reconstruir(IdentificacionApartamento identificacion, String nombre,
            TiempoPreparacion tiempoPreparacion, IdentificacionAlojamiento identificacionAlojamiento,
            Dormitorio dormitorios, CapacidadOcupantes capacidad, LimiteMascota limiteMascotas,
            List<String> caracteristicas, List<String> imagenes, EstadoOperativo estadoOperativo,
            boolean activo, List<Novedad> novedades) {
        Apartamento apartamento = new Apartamento(identificacion, nombre, tiempoPreparacion,
                identificacionAlojamiento, dormitorios, capacidad, limiteMascotas, caracteristicas, imagenes);
        apartamento.estadoOperativo = estadoOperativo;
        apartamento.activo = activo;
        apartamento.novedades.addAll(novedades);
        return apartamento;
    }

    // ------ COMPORTAMIENTO ------


    public void agregarImagen(String urlImagen) {
        if (urlImagen == null || urlImagen.isBlank()) {
            throw new ReglaDominioException("La URL de la imagen no puede estar vacía");
        }
        if (!this.imagenes.contains(urlImagen)) {
            this.imagenes.add(urlImagen);
        }
    }

    public void agregarCaracteristica(String caracteristica) {
        if (caracteristica == null || caracteristica.isBlank()) {
            throw new ReglaDominioException("La característica no puede estar vacía");
        }
        if (!this.caracteristicas.contains(caracteristica)) {
            this.caracteristicas.add(caracteristica);
        }
    }

    /**
     * Registra una novedad (daño, faltante o situación) reportada en el apartamento (CU-15).
     */
    public void registrarNovedad(Novedad novedad) {
        if (!activo) {
            throw new ReglaDominioException("No se pueden registrar novedades en un apartamento inactivo");
        }
        if (novedad == null) {
            throw new ReglaDominioException("La novedad a registrar es obligatoria");
        }
        this.novedades.add(novedad);
    }


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

    public void cambiarEstado(EstadoOperativo estado) {
        verificarEstado(estado);
        this.estadoOperativo = estado;
    }

    private void verificarEstado(EstadoOperativo estado) {
        if (!this.estadoOperativo.puedeTransicionarA(estado)) {
            throw new ReglaDominioException(
                    "El apartamento no puede pasar de " + this.estadoOperativo + " a " + estado);
        }
    }

    // ------ CONSULTAS -----

    /**
     * Devuelve una vista inmodificable de las novedades registradas en el apartamento.
     */
    public List<Novedad> getNovedades() {
        return Collections.unmodifiableList(novedades);
    }

    public IdentificacionApartamento getIdentificacion() {
        return identificacion;
    }

    public List<String> getCaracteristicas() {
        return Collections.unmodifiableList(caracteristicas);
    }

    public List<String> getImagenes() {
        return Collections.unmodifiableList(imagenes);
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