package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * RN-17: registro de quién autorizó cerrar un folio con saldo distinto de cero,
 * por qué y cuándo.
 */

public record AutorizacionCierre(String autor, String motivo, LocalDateTime fecha) {
    public AutorizacionCierre {
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("El autor de la autorización no puede estar vacío");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("El motivo de la autorización no puede estar vacío");
        }
        if (fecha == null) {
            throw new ReglaDominioException("La fecha de la autorización no puede ser nula");
        }
    }

}
