// Indicador de carga de Bootstrap; lo usan las páginas que piden datos al abrir
// (ListaReservas, MisReservas, BuscarDisponibilidad).

// Spinner centrado con texto para lectores de pantalla
export function Cargando() {
  return (
    <div className="d-flex justify-content-center my-5">
      <div className="spinner-border text-primary" role="status">
        <span className="visually-hidden">Cargando…</span>
      </div>
    </div>
  );
}
