package co.edu.uniquindio.sga.application.port;

import co.edu.uniquindio.sga.domain.entity.Usuario;

import java.time.Instant;

/**
 * Puerto para emitir el token de sesión. El caso de uso de inicio de sesión
 * no sabe que es un JWT: solo pide "dame un token para este usuario".
 */
public interface EmisorToken {

    TokenAcceso emitir(Usuario usuario);

    record TokenAcceso(String valor, Instant expiraEn) {
    }
}
