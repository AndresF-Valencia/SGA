package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.valueobject.Rol;

/**
 * Quién hace la petición, leído del token. Los controllers lo reciben con
 * {@code @AuthenticationPrincipal UsuarioAutenticado usuario}.
 */
public record UsuarioAutenticado(String correo, String nombre, String documento, Rol rol) {

    public boolean esHuesped() {
        return rol == Rol.HUESPED;
    }
}
