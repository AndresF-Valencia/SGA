package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador del puerto CifradorContrasena con BCrypt: nunca se guarda la contraseña en texto plano. */
@Component
public class CifradorContrasenaBCrypt implements CifradorContrasena {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String contrasenaPlana) {
        return bcrypt.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String contrasenaCifrada) {
        return bcrypt.matches(contrasenaPlana, contrasenaCifrada);
    }
}
