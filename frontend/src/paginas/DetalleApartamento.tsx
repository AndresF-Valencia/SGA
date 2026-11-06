// Página "/apartamentos/:identificacion": detalle de un apartamento de ejemplo.
// El parámetro de la URL es la misma identificación que usa el backend (AP001...).
import { Link, useParams } from 'react-router';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';

// Busca el apartamento por el parámetro de la ruta; si no existe, lo dice
export default function DetalleApartamento() {
  const { identificacion } = useParams();
  const apartamento = apartamentosEjemplo.find((a) => a.identificacion === identificacion);

  if (!apartamento) {
    return <div className="alert alert-warning">No existe el apartamento {identificacion}.</div>;
  }

  return (
    <section className="card shadow-sm">
      <div className="card-header d-flex justify-content-between align-items-center">
        <h1 className="h4 mb-0">{apartamento.nombre}</h1>
        <span className="badge text-bg-light border">{apartamento.identificacion}</span>
      </div>
      <div className="card-body">
        <p className="mb-2">
          <i className="bi bi-door-closed me-1" aria-hidden="true"></i>{apartamento.dormitorios} dormitorio(s)
          <span className="mx-2">·</span>
          <i className="bi bi-people me-1" aria-hidden="true"></i>hasta {apartamento.capacidadMaxima} personas
          <span className="mx-2">·</span>
          <i className="bi bi-heart me-1" aria-hidden="true"></i>hasta {apartamento.limiteMascotas} mascotas
        </p>
        <div className="d-flex flex-wrap gap-2">
          {apartamento.caracteristicas.map((c) => (
            <span key={c} className="badge rounded-pill text-bg-secondary">{c}</span>
          ))}
        </div>
      </div>
      <div className="card-footer d-flex gap-2">
        <Link to="/cotizacion" className="btn btn-primary btn-sm">Cotizar una estancia</Link>
        <Link to="/" className="btn btn-outline-secondary btn-sm">Volver</Link>
      </div>
    </section>
  );
}
