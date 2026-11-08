// Página "/apartamentos/:identificacion": detalle de un apartamento de ejemplo.
// El parámetro de la URL es la misma identificación que usa el backend (AP001...).
import { useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { Puerta } from '../componentes/Puerta';
import { miniatura } from '../utilidades/identidad';
import { enlaceCotizacion, enlaceReserva } from '../utilidades/enlaces';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { escucharCambiosApartamentos, obtenerApartamento } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';

// Busca el apartamento por el parámetro de la ruta; si no existe, lo dice
export default function DetalleApartamento() {
  const { identificacion } = useParams();
  // Si viene de la búsqueda, trae las fechas y personas en la URL
  const [parametros] = useSearchParams();
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

  const estancia = {
    fechaEntrada: parametros.get('fechaEntrada'),
    fechaSalida: parametros.get('fechaSalida'),
    personas: parametros.get('personas'),
  };
  const dormitorios = apartamento.dormitorios === 1 ? '1 dormitorio' : `${apartamento.dormitorios} dormitorios`;
  const mascotas = apartamento.limiteMascotas === 0
    ? 'No admite mascotas'
    : apartamento.limiteMascotas === 1 ? 'Admite 1 mascota' : `Admite hasta ${apartamento.limiteMascotas} mascotas`;

  return (
    <section className="row g-4">
      <div className="col-12 col-lg-8">
        <Link to="/" className="enlace-discreto small">Todos los apartamentos</Link>
        <div className="d-flex align-items-center gap-3 mt-2 mb-3">
          <Puerta identificacion={apartamento.identificacion} tamano="lg" />
          <div>
            <h1 className="h1 mb-1">{apartamento.nombre}</h1>
            <p className="text-body-secondary mb-0">{dormitorios}, hasta {apartamento.capacidadMaxima} personas</p>
          </div>
        </div>

        {apartamento.imagenes.length > 0 && (
          <div className={`galeria galeria-${Math.min(apartamento.imagenes.length, 3)}`}>
            {apartamento.imagenes.slice(0, 3).map((imagen, indice) => (
              <img key={imagen} src={indice === 0 ? imagen : miniatura(imagen, 900)}
                alt={`${apartamento.nombre}, foto ${indice + 1}`} loading={indice === 0 ? 'eager' : 'lazy'} />
            ))}
          </div>
        )}

        {apartamento.descripcion && <p className="fs-5" style={{ maxWidth: '62ch' }}>{apartamento.descripcion}</p>}

        <h2 className="h4 mt-4">Qué incluye</h2>
        <ul className="lista-incluye">
          {apartamento.caracteristicas.map((c) => <li key={c}>{c}</li>)}
          <li>{mascotas}</li>
        </ul>
      </div>

      <aside className="col-12 col-lg-4">
        <div className="card shadow-sm">
          <div className="card-body">
            <h2 className="h4">¿Te sirve este apartamento?</h2>
            <p className="text-body-secondary">Calcula el valor exacto de tu estancia, noche por noche, antes de reservar.</p>
            <div className="d-grid gap-2">
              <Link to={enlaceCotizacion({ ...estancia, apartamento: apartamento.identificacion })} className="btn btn-primary">Cotizar mi estancia</Link>
              <Link to={enlaceReserva({ ...estancia, apartamento: apartamento.identificacion })} className="btn btn-outline-secondary">Reservar este apartamento</Link>
            </div>
            {!apartamento.activo && (
              <p className="alert alert-warning mt-3 mb-0">Este apartamento no está recibiendo reservas por ahora.</p>
            )}
          </div>
        </div>
      </aside>
    </section>
  );
}
