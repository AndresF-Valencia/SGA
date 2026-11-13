package co.edu.uniquindio.sga.application.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record NotificarRetrasoRequest(
        @NotNull(message = "La nueva fecha y hora estimada de llegada es obligatoria")
        LocalTime nuevaHoraEstimadaLlegada,

        @NotNull(message = "Debe registrar el motivo del retraso")
        @Size(max = 500, message = "El motivo o detalle del retraso no puede superar los 500 caracteres")
        String motivo
) {}