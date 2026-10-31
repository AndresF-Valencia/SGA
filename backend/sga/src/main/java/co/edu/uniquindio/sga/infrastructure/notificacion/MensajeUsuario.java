package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.domain.entity.Usuario;

/** Textos de los correos sobre la cuenta del usuario. */
final class MensajeUsuario {

    static final String ASUNTO_RECUPERACION = "San Rocco · Código para recuperar tu contraseña";

    private MensajeUsuario() {
    }

    static String cuerpoRecuperacion(Usuario usuario, String codigo, int minutosVigencia) {
        return "Hola " + usuario.getNombre() + ",\n\n"
                + "Tu código para recuperar la contraseña es: " + codigo + "\n\n"
                + "Vence en " + minutosVigencia + " minutos y solo se puede usar una vez.\n"
                + "Si no lo pediste, ignora este correo: tu contraseña sigue siendo la misma.\n\n"
                + "Apartamentos San Rocco";
    }
}
