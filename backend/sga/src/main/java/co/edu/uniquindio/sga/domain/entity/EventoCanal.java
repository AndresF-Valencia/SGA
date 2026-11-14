package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Evento de la bitácora de canales (7.8): una línea por cada mensaje que un canal externo
 * intercambia con el sistema (consulta de disponibilidad, reserva externa o cancelación),
 * con su resultado. Sirve para revisar después qué pasó con un canal.
 *
 * Es un registro histórico: una vez creado no cambia ni se borra.
 */
public class EventoCanal {

    /** Operaciones del contrato del canal externo (10.3.1). */
    public static final String CONSULTA_DISPONIBILIDAD = "CONSULTA_DISPONIBILIDAD";
    public static final String RESERVA_EXTERNA = "RESERVA_EXTERNA";
    public static final String CANCELACION_EXTERNA = "CANCELACION_EXTERNA";

    private static final int LARGO_DETALLE = 500;

    private final String id;
    private final String canal;          // nombre del canal, o "Sin identificar" si la credencial no era válida
    private final String operacion;
    private final String codigoExterno;  // nulo en las consultas de disponibilidad
    private final boolean exitoso;
    private final String detalle;        // qué se pidió y, si falló, por qué
    private final LocalDateTime ocurridoEn;

    private EventoCanal(String id, String canal, String operacion, String codigoExterno, boolean exitoso,
            String detalle, LocalDateTime ocurridoEn) {
        this.id = id;
        this.canal = canal;
        this.operacion = operacion;
        this.codigoExterno = codigoExterno;
        this.exitoso = exitoso;
        this.detalle = detalle;
        this.ocurridoEn = ocurridoEn;
    }

    public static EventoCanal registrar(String id, String canal, String operacion, String codigoExterno,
            boolean exitoso, String detalle, LocalDateTime ocurridoEn) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("El evento de canal debe tener identificador");
        }
        if (canal == null || canal.isBlank()) {
            throw new ReglaDominioException("El evento de canal debe indicar el canal");
        }
        if (operacion == null || operacion.isBlank()) {
            throw new ReglaDominioException("El evento de canal debe indicar la operación");
        }
        if (ocurridoEn == null) {
            throw new ReglaDominioException("El evento de canal debe indicar cuándo ocurrió");
        }
        String texto = detalle == null ? "" : detalle.trim();
        if (texto.length() > LARGO_DETALLE) {
            texto = texto.substring(0, LARGO_DETALLE);
        }
        return new EventoCanal(id, canal, operacion, codigoExterno, exitoso, texto, ocurridoEn);
    }

    public String getId() {
        return id;
    }

    public String getCanal() {
        return canal;
    }

    public String getOperacion() {
        return operacion;
    }

    public String getCodigoExterno() {
        return codigoExterno;
    }

    public boolean fueExitoso() {
        return exitoso;
    }

    public String getDetalle() {
        return detalle;
    }

    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
