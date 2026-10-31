package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Conflicto de canal (glosario, 7.8, RN-18): una reserva externa que se rechazó porque
 * chocaba con una reserva o un bloqueo vigente. Queda guardado para que el administrador
 * lo revise. La reserva existente nunca se toca.
 *
 * Nace PENDIENTE; el administrador lo marca RESUELTO dejando por escrito qué hizo
 * (por ejemplo, "se ofreció AP004 al huésped por el canal").
 */
public class ConflictoCanal {

    private final String id;
    private final String canal;               // nombre del canal que envió la reserva
    private final String codigoExterno;
    private final String apartamento;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final String titularNombre;
    private final String titularDocumento;
    private final String motivo;              // por qué se rechazó
    private final LocalDateTime recibidoEn;
    private boolean resuelto;
    private String resolucion;
    private String resueltoPor;
    private LocalDateTime resueltoEn;

    private ConflictoCanal(String id, String canal, String codigoExterno, String apartamento,
            LocalDate fechaEntrada, LocalDate fechaSalida, String titularNombre, String titularDocumento,
            String motivo, LocalDateTime recibidoEn) {
        this.id = id;
        this.canal = canal;
        this.codigoExterno = codigoExterno;
        this.apartamento = apartamento;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.titularNombre = titularNombre;
        this.titularDocumento = titularDocumento;
        this.motivo = motivo;
        this.recibidoEn = recibidoEn;
    }

    public static ConflictoCanal registrar(String id, String canal, String codigoExterno, String apartamento,
            LocalDate fechaEntrada, LocalDate fechaSalida, String titularNombre, String titularDocumento,
            String motivo, LocalDateTime recibidoEn) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("El conflicto debe tener identificador");
        }
        if (canal == null || canal.isBlank() || codigoExterno == null || codigoExterno.isBlank()) {
            throw new ReglaDominioException("El conflicto debe indicar el canal y el código externo");
        }
        if (motivo == null || motivo.isBlank() || recibidoEn == null) {
            throw new ReglaDominioException("El conflicto debe indicar el motivo y cuándo se recibió");
        }
        return new ConflictoCanal(id, canal, codigoExterno, apartamento, fechaEntrada, fechaSalida,
                titularNombre, titularDocumento, motivo, recibidoEn);
    }

    /** Lo usa la persistencia. */
    public static ConflictoCanal reconstruir(String id, String canal, String codigoExterno, String apartamento,
            LocalDate fechaEntrada, LocalDate fechaSalida, String titularNombre, String titularDocumento,
            String motivo, LocalDateTime recibidoEn, boolean resuelto, String resolucion, String resueltoPor,
            LocalDateTime resueltoEn) {
        ConflictoCanal c = new ConflictoCanal(id, canal, codigoExterno, apartamento, fechaEntrada, fechaSalida,
                titularNombre, titularDocumento, motivo, recibidoEn);
        c.resuelto = resuelto;
        c.resolucion = resolucion;
        c.resueltoPor = resueltoPor;
        c.resueltoEn = resueltoEn;
        return c;
    }

    public void resolver(String resolucion, String autor, LocalDateTime ahora) {
        if (resuelto) {
            throw new ReglaDominioException("El conflicto ya fue resuelto");
        }
        if (resolucion == null || resolucion.isBlank()) {
            throw new ReglaDominioException("Debe indicarse cómo se resolvió el conflicto");
        }
        this.resuelto = true;
        this.resolucion = resolucion.trim();
        this.resueltoPor = autor;
        this.resueltoEn = ahora;
    }

    public String getId() { return id; }
    public String getCanal() { return canal; }
    public String getCodigoExterno() { return codigoExterno; }
    public String getApartamento() { return apartamento; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public String getTitularNombre() { return titularNombre; }
    public String getTitularDocumento() { return titularDocumento; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getRecibidoEn() { return recibidoEn; }
    public boolean estaResuelto() { return resuelto; }
    public String getResolucion() { return resolucion; }
    public String getResueltoPor() { return resueltoPor; }
    public LocalDateTime getResueltoEn() { return resueltoEn; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ConflictoCanal otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
