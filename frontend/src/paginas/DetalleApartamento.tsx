// Página "/apartamentos/:identificacion": detalle de un apartamento de ejemplo.
// El parámetro de la URL es la misma identificación que usa el backend (AP001...).
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { escucharCambiosApartamentos, obtenerApartamento } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';

// Busca el apartamento por el parámetro de la ruta; si no existe, lo dice
export default function DetalleApartamento() {
  const { identificacion } = useParams();
  const [apartamento, setApartamento] = useState<ApartamentoResumen | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!identificacion) return;
    const cargar = () => {
      setCargando(true);
      setError(null);
      obtenerApartamento(identificacion).then(setApartamento).catch((e: unknown) => setError(mensajeDeError(e))).finally(() => setCargando(false));
    };
    cargar();
    return escucharCambiosApartamentos(cargar);
  }, [identificacion]);

  if (cargando) return <Cargando />;
  if (error) return <div className="alert alert-danger">{error}</div>;

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
        {apartamento.imagenes.length > 0 && (
          <div className="row g-2 mb-3">
            {apartamento.imagenes.map((imagen) => (
              <div key={imagen} className="col-12 col-md-6">
                <img src={imagen} alt={apartamento.nombre} className="img-fluid rounded" />
              </div>
            ))}
          </div>
        )}
        {apartamento.descripcion && <p>{apartamento.descripcion}</p>}
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
        <p className="mt-3 mb-0">
          <span className={`badge ${apartamento.activo ? 'text-bg-success' : 'text-bg-secondary'}`}>
            {apartamento.activo ? 'Activo' : 'No disponible'}
          </span>
          <span className="badge text-bg-light border ms-2">{apartamento.estadoOperativo}</span>
        </p>
      </div>
      <div className="card-footer d-flex gap-2">
        <Link to="/cotizacion" className="btn btn-primary btn-sm">Cotizar una estancia</Link>
        <Link to="/" className="btn btn-outline-secondary btn-sm">Volver</Link>
      </div>
    </section>
  );
}
