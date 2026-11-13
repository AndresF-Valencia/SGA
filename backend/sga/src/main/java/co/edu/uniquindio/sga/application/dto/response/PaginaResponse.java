package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

/**
 * DTO de respuesta genérico para todos los listados paginados (Guía 07, sección 5.9).
 * Mapea a las respuestas de los endpoints de consulta que devuelven colecciones, por ejemplo:
 * - GET /api/reservas
 * - GET /api/temporadas
 * - GET /api/politicas-cancelacion
 *
 * Nota de diseño: la sección 12.4 del proyecto exige paginación de 10 resultados por página
 * en todos los listados. Por eso el tamaño por defecto es una constante del contrato y no
 * el valor por defecto de Spring (20).
 *
 * @param <T> tipo de los elementos de la página
 */
public record PaginaResponse<T>(

        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas

) {

    /** Tamaño de página obligatorio en todo el proyecto (12.4). */
    public static final int TAMANO_POR_DEFECTO = 10;

    /**
     * Corta una lista completa en la página pedida usando siempre y estrictamente el tamaño por defecto (10).
     */
    public static <T> PaginaResponse<T> de(List<T> todos, int pagina, int tamano) {
        int tam = TAMANO_POR_DEFECTO; // Forzado siempre a 10 (12.4)
        int pag = Math.max(pagina, 0);
        int desde = (int) Math.min((long) pag * tam, todos.size());
        int hasta = Math.min(desde + tam, todos.size());
        int totalPaginas = (todos.size() + tam - 1) / tam;
        return new PaginaResponse<>(todos.subList(desde, hasta), pag, tam, todos.size(), totalPaginas);
    }

    /**
     * Sobrecarga práctica para construir la página omitiendo el parámetro de tamaño.
     */
    public static <T> PaginaResponse<T> de(List<T> todos, int pagina) {
        return de(todos, pagina, TAMANO_POR_DEFECTO);
    }
}