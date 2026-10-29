package co.edu.uniquindio.sga.infrastructure.config;

import co.edu.uniquindio.sga.application.usecase.CrearUsuarioInternoUseCase;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 12.3: al iniciar debe existir al menos un usuario por rol.
 * Sin esto nadie podría entrar como administrador para crear a los demás.
 *
 * Se apaga con sga.datos-prueba.habilitado=false (por ejemplo, en despliegue).
 * La contraseña es de PRUEBA, no un secreto real: se puede cambiar con SGA_CLAVE_PRUEBA.
 */
@Component
public class UsuariosDePrueba implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuariosDePrueba.class);

    private final CrearUsuarioInternoUseCase crearUsuario;
    private final UsuarioRepository usuarioRepository;
    private final boolean habilitado;
    private final String clave;

    public UsuariosDePrueba(CrearUsuarioInternoUseCase crearUsuario, UsuarioRepository usuarioRepository,
                            @Value("${sga.datos-prueba.habilitado:true}") boolean habilitado,
                            @Value("${sga.datos-prueba.clave:Sanrocco123}") String clave) {
        this.crearUsuario = crearUsuario;
        this.usuarioRepository = usuarioRepository;
        this.habilitado = habilitado;
        this.clave = clave;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!habilitado) {
            return;
        }
        crear("admin@sanrocco.co", "Administrador San Rocco", "1000000001", Rol.ADMINISTRADOR);
        crear("recepcion@sanrocco.co", "Recepción San Rocco", "1000000002", Rol.RECEPCIONISTA);
        crear("servicio@sanrocco.co", "Personal de Servicio", "1000000003", Rol.PERSONAL_SERVICIO);
        crear("huesped@sanrocco.co", "Laura Gómez", "1094000001", Rol.HUESPED);
    }

    private void crear(String correo, String nombre, String documento, Rol rol) {
        if (usuarioRepository.existePorCorreo(new Correo(correo))) {
            return;
        }
        crearUsuario.ejecutar(new Correo(correo), nombre, clave, "3000000000",
                new DocumentoIdentidad(documento), LocalDate.of(1995, 1, 1), rol, LocalDate.now());
        log.info("Usuario de prueba creado: {} ({})", correo, rol);
    }
}
