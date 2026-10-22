package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalTime;

/**
 * Raíz del agregado Alojamiento: representa el negocio completo (F-13).
 * Administra la configuración global, parámetros y normas operativas.
 */
public class Alojamiento {

    private final IdentificacionAlojamiento identificacion;
    private final String nombre;
    private final String ciudad;
    private final String descripcion;

    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private LocalTime horaLimiteNoShow;
    private UmbralEdadFacturable umbralEdadFacturable;
    private TiempoPreparacion tiempoPreparacion;
    private PlazoConfirmacion plazoConfirmacion;
    private boolean exigeAnticipo;
    private int porcentajeAnticipo; // Ej: 50%
    // F-07: una sola política de cancelación, versionada. Cada reserva congela la
    // vigente (RN-22).
    private VersionPolitica versionPoliticaVigente = new VersionPolitica(1);

    public Alojamiento(IdentificacionAlojamiento identificacion, String nombre, String ciudad,
            String descripcion, LocalTime horaEntrada, LocalTime horaSalida,
            LocalTime horaLimiteNoShow, UmbralEdadFacturable umbralEdadFacturable,
            TiempoPreparacion tiempoPreparacion, PlazoConfirmacion plazoConfirmacion,
            boolean exigeAnticipo, int porcentajeAnticipo) {
        if (identificacion == null) {
            throw new ReglaDominioException("El alojamiento debe tener una identificación");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El alojamiento debe tener un nombre");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new ReglaDominioException("El alojamiento debe indicar su ciudad");
        }
        if (horaEntrada == null || horaSalida == null) {
            throw new ReglaDominioException("El alojamiento debe definir las horas de entrada y salida");
        }
        if (umbralEdadFacturable == null) {
            throw new ReglaDominioException("El alojamiento debe definir el umbral de edad facturable");
        }
        if (tiempoPreparacion == null) {
            throw new ReglaDominioException("El alojamiento debe definir el tiempo de preparación");
        }
        if (plazoConfirmacion == null) {
            throw new ReglaDominioException("El alojamiento debe definir el plazo de confirmación");
        }
        if (exigeAnticipo && (porcentajeAnticipo <= 0 || porcentajeAnticipo > 100)) {
            throw new ReglaDominioException("El porcentaje de anticipo debe estar entre 1 y 100");
        }

        this.identificacion = identificacion;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.descripcion = descripcion;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.horaLimiteNoShow = horaLimiteNoShow;
        this.umbralEdadFacturable = umbralEdadFacturable;
        this.tiempoPreparacion = tiempoPreparacion;
        this.plazoConfirmacion = plazoConfirmacion;
        this.exigeAnticipo = exigeAnticipo;
        this.porcentajeAnticipo = porcentajeAnticipo;
    }

    // ----- COMPORTAMIENTO DE CONFIGURACIÓN -----

    public void actualizarParametrosOperativos(LocalTime horaEntrada, LocalTime horaSalida,
            TiempoPreparacion tiempoPreparacion,
            PlazoConfirmacion plazoConfirmacion) {
        if (horaEntrada == null || horaSalida == null || tiempoPreparacion == null || plazoConfirmacion == null) {
            throw new ReglaDominioException("Los parámetros operativos no pueden ser nulos");
        }
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.tiempoPreparacion = tiempoPreparacion;
        this.plazoConfirmacion = plazoConfirmacion;
    }

    public void actualizarConfiguracionAnticipo(boolean exigeAnticipo, int porcentajeAnticipo) {
        if (exigeAnticipo && (porcentajeAnticipo <= 0 || porcentajeAnticipo > 100)) {
            throw new ReglaDominioException("El porcentaje de anticipo debe estar entre 1 y 100");
        }
        this.exigeAnticipo = exigeAnticipo;
        this.porcentajeAnticipo = porcentajeAnticipo;
    }

    public void actualizarPoliticasGenerales(LocalTime horaLimiteNoShow, UmbralEdadFacturable umbralEdadFacturable) {
        if (umbralEdadFacturable == null) {
            throw new ReglaDominioException("El umbral de edad facturable es obligatorio");
        }
        this.horaLimiteNoShow = horaLimiteNoShow;
        this.umbralEdadFacturable = umbralEdadFacturable;
    }

    // ----- CONSULTAS -----

    public IdentificacionAlojamiento getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public LocalTime getHoraLimiteNoShow() {
        return horaLimiteNoShow;
    }

    public UmbralEdadFacturable getUmbralEdadFacturable() {
        return umbralEdadFacturable;
    }

    public TiempoPreparacion getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public PlazoConfirmacion getPlazoConfirmacion() {
        return plazoConfirmacion;
    }

    public boolean isExigeAnticipo() {
        return exigeAnticipo;
    }

    /**
     * 7.4: al actualizar la política se conserva la anterior y se publica una
     * versión nueva.
     */
    public VersionPolitica publicarNuevaVersionPolitica() {
        this.versionPoliticaVigente = new VersionPolitica(versionPoliticaVigente.numero() + 1);
        return versionPoliticaVigente;
    }

    public VersionPolitica getVersionPoliticaVigente() {
        return versionPoliticaVigente;
    }

    public int getPorcentajeAnticipo() {
        return porcentajeAnticipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Alojamiento otro))
            return false;
        return this.identificacion.equals(otro.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}