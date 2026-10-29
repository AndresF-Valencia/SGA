package co.edu.uniquindio.sga.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Se ejecuta antes de cada petición: si trae "Authorization: Bearer <token>" y
 * el token es válido, le dice a Spring quién es el usuario y cuál es su rol.
 * Si no trae token o es inválido, la petición sigue como anónima y la
 * SecurityConfig decide si la deja pasar (rutas públicas) o responde 401.
 *
 * No lleva @Component a propósito: lo crea SecurityConfig, para que solo
 * corra dentro de la cadena de seguridad.
 */
public class FiltroJwt extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtServicio jwtServicio;

    public FiltroJwt(JwtServicio jwtServicio) {
        this.jwtServicio = jwtServicio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            jwtServicio.validar(encabezado.substring(PREFIJO.length())).ifPresent(usuario -> {
                // Spring espera los roles con el prefijo "ROLE_" para hasRole(...)
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        cadena.doFilter(request, response);
    }
}
