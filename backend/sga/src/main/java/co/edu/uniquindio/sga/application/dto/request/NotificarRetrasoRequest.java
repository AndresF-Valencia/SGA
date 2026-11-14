package co.edu.uniquindio.sga.application.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record NotificarRetrasoRequest(
        @NotNull(message = "La nueva fecha y hora estimada de llegada es obligatoria")
        LocalTime nuevaHoraEstimadaLlegada,

        @NotNull(message = "Debe registrar el motivo del retraso")
        @Size(max = 300, message = "El motivo del retraso no puede superar los 300 caracteres")
        String motivo
) {}