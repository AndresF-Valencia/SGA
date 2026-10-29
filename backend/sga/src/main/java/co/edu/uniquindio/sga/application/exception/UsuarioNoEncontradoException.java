package co.edu.uniquindio.sga.application.exception;

import co.edu.uniquindio.sga.domain.valueobject.Correo;

/** Excepción de aplicación: no existe un usuario con ese correo (Guía 06) */
public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(Correo correo) {
        super("No se encontró el usuario con correo: " + correo.valor());
    }
}
