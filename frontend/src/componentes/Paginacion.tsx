interface PaginacionProps {
  paginaActual: number;
  totalPaginas: number;
  alCambiar: (pagina: number) => void;
}

export function Paginacion({ paginaActual, totalPaginas, alCambiar }: PaginacionProps) {
  if (totalPaginas <= 1) return null;

  return (
    <nav aria-label="Paginación de reservas" className="mt-4">
      <ul className="pagination justify-content-center">
        <li className={`page-item ${paginaActual === 0 ? 'disabled' : ''}`}>
          <button
            type="button"
            className="page-link"
            disabled={paginaActual === 0}
            onClick={() => alCambiar(paginaActual - 1)}
          >
            Anterior
          </button>
        </li>
        <li className="page-item active" aria-current="page">
          <span className="page-link">{paginaActual + 1} de {totalPaginas}</span>
        </li>
        <li className={`page-item ${paginaActual >= totalPaginas - 1 ? 'disabled' : ''}`}>
          <button
            type="button"
            className="page-link"
            disabled={paginaActual >= totalPaginas - 1}
            onClick={() => alCambiar(paginaActual + 1)}
          >
            Siguiente
          </button>
        </li>
      </ul>
    </nav>
  );
}