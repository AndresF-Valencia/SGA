package co.edu.uniquindio.sga.domain.port;

/**
 * Puerto para cifrar contraseñas (7.2: "contraseña encriptada").
 * El dominio no sabe si se usa BCrypt u otro algoritmo; eso lo decide infraestructura.
 */
public interface CifradorContrasena {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String contrasenaCifrada);
}
