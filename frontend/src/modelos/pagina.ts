// Espejo de PaginaResponse.java: la forma de todos los listados paginados del backend
// (reservas, mis reservas, usuarios, temporadas...). El backend limita el tamaño a 10.
export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export const TAMANO_PAGINA = 10;
