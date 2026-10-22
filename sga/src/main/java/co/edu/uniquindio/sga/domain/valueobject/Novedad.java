package co.edu.uniquindio.sga.domain.valueobject;

import java.time.LocalDateTime;
import java.util.Objects;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Objeto de valor que encapsula los detalles de una novedad reportada 
 * en un apartamento (daño, faltante o situación) (CU-15).
 */
public final class Novedad {

    private final String descripcion;
    private final String gravedad;
    private final String autor;
    private final LocalDateTime fecha;

    private Novedad(String descripcion, String gravedad, String autor, LocalDateTime fecha) {
        this.descripcion = descripcion;
        this.gravedad = gravedad;
        this.autor = autor;
        this.fecha = fecha;
    }

    /**
     * Única puerta de entrada para crear una novedad validando sus invariantes.
     */
    public static Novedad de(String descripcion, String gravedad, String autor, LocalDateTime fecha) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("La descripción de la novedad es obligatoria");
        }
        if (gravedad == null || gravedad.isBlank()) {
            throw new ReglaDominioException("La gravedad de la novedad es obligatoria");
        }
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("El autor del reporte es obligatorio");
        }
        if (fecha == null) {
            throw new ReglaDominioException("La fecha de la novedad es obligatoria");
        }
        return new Novedad(descripcion, gravedad, autor, fecha);
    }

    public String descripcion() {
        return descripcion;
    }

    public String gravedad() {
        return gravedad;
    }

    public String autor() {
        return autor;
    }

    public LocalDateTime fecha() {
        return fecha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Novedad otra)) return false;
        return descripcion.equals(otra.descripcion) &&
                gravedad.equals(otra.gravedad) &&
                autor.equals(otra.autor) &&
                fecha.equals(otra.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descripcion, gravedad, autor, fecha);
    }
}