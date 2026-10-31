package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/** 7.2: el administrador asigna el rol de un usuario. */
public record CambiarRolRequest(

        @NotBlank(message = "El rol es obligatorio: HUESPED, RECEPCIONISTA, ADMINISTRADOR o PERSONAL_SERVICIO")
        String rol

) {}
