package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.regex.Pattern;

/**
 * Correo electrónico de un usuario (7.2). Es único y no se puede editar,
 * por eso también sirve como identificador del usuario.
 * Se guarda en minúsculas para que "Ana@Mail.com" y "ana@mail.com" sean el mismo.
 */
public record Correo(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Correo {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El correo es obligatorio");
        }
        valor = valor.trim().toLowerCase();
        if (!FORMATO.matcher(valor).matches()) {
            throw new ReglaDominioException("El correo no tiene un formato válido");
        }
    }
}
