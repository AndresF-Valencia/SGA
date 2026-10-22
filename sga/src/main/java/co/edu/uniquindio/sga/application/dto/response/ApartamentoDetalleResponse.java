package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/**
 * DTO de respuesta con el detalle completo de un apartamento (Guía 07, sección 5.9; proyecto 7.3).
 * Mapea a la respuesta de:
 * - GET /api/apartamentos/{identificacion}
 *
 * Nota de diseño: incluye capacidad, dotación, estado operativo e imágenes, que es lo que el
 * huésped necesita para ver el detalle antes de cotizar (7.1). Las imágenes son URLs del
 * servicio externo de almacenamiento (12.2); la primera es la imagen principal.
 */
public record ApartamentoDetalleResponse(

        String identificacion,
        String nombre,
        String descripcion,
        int dormitorios,
        int capacidadMaxima,
        int limiteMascotas,
        String estadoOperativo,
        boolean activo,
        List<String> caracteristicas,
        List<String> imagenes

) {}
