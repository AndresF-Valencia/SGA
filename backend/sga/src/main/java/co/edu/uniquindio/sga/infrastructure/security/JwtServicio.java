package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.port.EmisorToken;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/**
 * Crea y valida los JWT. Es el adaptador del puerto EmisorToken.
 *
 * El token lleva el correo (subject), el nombre, el documento y el rol, firmado
 * con una clave secreta. Si alguien cambia algo del token, la firma deja de
 * coincidir y el token se rechaza.
 *
 * La clave NO se versiona (12.4): se lee de la variable de entorno SGA_JWT_SECRETO.
 * Si no está configurada, se genera una al azar al arrancar (sirve para desarrollo,
 * pero los tokens dejan de valer cada vez que se reinicia la aplicación).
 */
@Component
public class JwtServicio implements EmisorToken {

    private static final Logger log = LoggerFactory.getLogger(JwtServicio.class);

    private final SecretKey clave;
    private final long minutosVigencia;

    public JwtServicio(@Value("${sga.jwt.secreto:}") String secreto,
                       @Value("${sga.jwt.expiracion-minutos:120}") long minutosVigencia) {
        this.clave = Keys.hmacShaKeyFor(bytesDeLaClave(secreto));
        this.minutosVigencia = minutosVigencia;
    }

    private static byte[] bytesDeLaClave(String secreto) {
        if (secreto == null || secreto.isBlank()) {
            log.warn("SGA_JWT_SECRETO no está configurado: se usará una clave temporal. "
                    + "Los tokens dejarán de valer al reiniciar la aplicación.");
            byte[] aleatoria = new byte[64];
            new SecureRandom().nextBytes(aleatoria);
            return aleatoria;
        }
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("SGA_JWT_SECRETO debe tener al menos 32 caracteres");
        }
        return bytes;
    }

    @Override
    public TokenAcceso emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(minutosVigencia, ChronoUnit.MINUTES);
        String token = Jwts.builder()
                .subject(usuario.getCorreo().valor())
                .claim("nombre", usuario.getNombre())
                .claim("documento", usuario.getDocumento().numero())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();
        return new TokenAcceso(token, expira);
    }

    /** Devuelve quién es el usuario si el token es válido y no ha vencido; vacío si no. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims datos = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new UsuarioAutenticado(
                    datos.getSubject(),
                    datos.get("nombre", String.class),
                    datos.get("documento", String.class),
                    Rol.valueOf(datos.get("rol", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
