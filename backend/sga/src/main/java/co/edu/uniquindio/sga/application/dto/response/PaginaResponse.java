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
     * Corta una lista completa en la página pedida; página y tamaño fuera de rango se ajustan.
     *
     * Por qué existe: el record define el contrato de todos los listados paginados (12.4),
     * pero nadie lo construía. Con este método cada controlador arma la página en una sola
     * línea, en vez de repetir la aritmética de subList, totalElementos y totalPaginas, y
     * todos respetan el mismo tamaño máximo de 10.
     *
     * Cómo complementa a las otras clases: el caso de uso devuelve la lista completa del
     * dominio (sin saber de páginas), el controlador la mapea a su DTO de respuesta y se la
     * pasa a este método. Lo usa GET /api/temporadas y sirve igual para el historial de
     * tarifas, las políticas de cancelación y las novedades. Si algún día la lista deja de
     * caber en memoria, solo habría que reemplazar este método por paginación en el
     * repositorio, sin cambiar el JSON que ve el cliente.
     */
    public static <T> PaginaResponse<T> de(List<T> todos, int pagina, int tamano) {
        int tam = Math.min(Math.max(tamano, 1), TAMANO_POR_DEFECTO);
        int pag = Math.max(pagina, 0);
        int desde = (int) Math.min((long) pag * tam, todos.size());
        int hasta = Math.min(desde + tam, todos.size());
        int totalPaginas = (todos.size() + tam - 1) / tam;
        return new PaginaResponse<>(todos.subList(desde, hasta), pag, tam, todos.size(), totalPaginas);
    }
}
