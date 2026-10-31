package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Conflicto de canal registrado (7.8). */
public record ConflictoCanalResponse(
        String id,
        String canal,
        String codigoExterno,
        String apartamentoId,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String titularNombre,
        String titularDocumento,
        String motivo,
        LocalDateTime recibidoEn,
        String estado,
        String resolucion,
        String resueltoPor,
        LocalDateTime resueltoEn
) {}
