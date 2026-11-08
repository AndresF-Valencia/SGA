import { useEffect, useState } from 'react';
import { Cargando } from '../componentes/Cargando';
import type { Alojamiento as AlojamientoModelo } from '../modelos/alojamiento';
import { obtenerAlojamiento } from '../servicios/alojamientoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { formatearPesos } from '../utilidades/formato';

export default function Alojamiento() {
  const [alojamiento, setAlojamiento] = useState<AlojamientoModelo | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    obtenerAlojamiento()
      .then(setAlojamiento)
      .catch((e: unknown) => setError(mensajeDeError(e)));
  }, []);

  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!alojamiento) return <Cargando />;

  return (
    <section>
      <h1 className="h3 mb-3">{alojamiento.nombre}</h1>
      <p className="lead">{alojamiento.descripcion}</p>
      <p><i className="bi bi-geo-alt me-2" aria-hidden="true"></i>{alojamiento.direccion}, {alojamiento.ciudad}</p>

      <div className="row g-3">
        <div className="col-12 col-lg-6">
          <article className="card h-100">
            <div className="card-header">Información de la estancia</div>
            <div className="card-body">
              <p className="mb-2">Entrada: <strong>{alojamiento.horaEntrada.slice(0, 5)}</strong></p>
              <p className="mb-2">Salida: <strong>{alojamiento.horaSalida.slice(0, 5)}</strong></p>
              <p className="mb-2">Edad mínima facturable: <strong>{alojamiento.edadUmbralFacturable} años</strong></p>
              <p className="mb-0">Anticipo: <strong>{alojamiento.exigeAnticipo ? `${alojamiento.porcentajeAnticipo}%` : 'No requerido'}</strong></p>
            </div>
          </article>
        </div>
        <div className="col-12 col-lg-6">
          <article className="card h-100">
            <div className="card-header">Servicios adicionales</div>
            <ul className="list-group list-group-flush">
              {alojamiento.serviciosAdicionales.map((servicio) => (
                <li key={servicio.nombre} className="list-group-item d-flex justify-content-between">
                  <span>{servicio.nombre}</span><strong>{formatearPesos(servicio.valor)}</strong>
                </li>
              ))}
            </ul>
          </article>
        </div>
        <div className="col-12">
          <article className="card">
            <div className="card-header">Normas de convivencia</div>
            <div className="card-body"><p className="mb-0">{alojamiento.normasConvivencia}</p></div>
          </article>
        </div>
      </div>
    </section>
  );
}