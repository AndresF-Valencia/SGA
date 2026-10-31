package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 7.2: datos editables del perfil. El correo no se edita. La foto es opcional (URL). */
public record ActualizarPerfilRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,

        @Size(max = 500, message = "La URL de la foto no puede superar 500 caracteres")
        String fotoPerfil

) {}
