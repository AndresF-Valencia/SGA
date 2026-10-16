package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionCanal;

/**
 * Entidad Canal: representa un canal de venta habilitado (portal, directo o externo)
 * con sus credenciales de acceso para integraciones (7.8, 10.3.1).
 */
public class Canal {

    private final IdentificacionCanal identificacion;
    private final CanalOrigen tipoCanal;
    private final String nombre;
    private final String credencialAcceso;
    private boolean activo;

    public Canal(IdentificacionCanal identificacion, CanalOrigen tipoCanal,
                 String nombre, String credencialAcceso) {
        if (identificacion == null) {
            throw new ReglaDominioException("El canal debe tener una identificación");
        }
        if (tipoCanal == null) {
            throw new ReglaDominioException("El canal debe indicar su tipo de origen");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El canal debe tener un nombre");
        }
        if (credencialAcceso == null || credencialAcceso.isBlank()) {
            throw new ReglaDominioException("El canal debe contar con una credencial de acceso");
        }

        this.identificacion = identificacion;
        this.tipoCanal = tipoCanal;
        this.nombre = nombre;
        this.credencialAcceso = credencialAcceso;
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    public IdentificacionCanal getIdentificacion() {
        return identificacion;
    }

    public CanalOrigen getTipoCanal() {
        return tipoCanal;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCredencialAcceso() {
        return credencialAcceso;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Canal otro)) return false;
        return this.identificacion.equals(otro.identificacion);
    }

    @Override
    public int hashCode() {
        return identificacion.hashCode();
    }
}