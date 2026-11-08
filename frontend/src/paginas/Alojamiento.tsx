// Página "/alojamiento" (pública): datos del alojamiento (GET /api/alojamiento) en formato editorial:
// horarios grandes, reglas de la estancia, carta de servicios y normas de convivencia.
import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import type { Alojamiento as AlojamientoModelo } from '../modelos/alojamiento';
import { obtenerAlojamiento } from '../servicios/alojamientoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { formatearPesos } from '../utilidades/formato';

// "15:00:00" → "3:00 p. m."
function horaLegible(hora: string): string {
  const [h, m] = hora.split(':').map(Number);
  const sufijo = h < 12 ? 'a. m.' : 'p. m.';
  const h12 = h % 12 === 0 ? 12 : h % 12;
  return `${h12}:${String(m).padStart(2, '0')} ${sufijo}`;
}

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
    <section className="alojamiento">
      <header className="alojamiento-cabecera">
        <h1>{alojamiento.nombre}</h1>
        <p className="lead mb-2">{alojamiento.descripcion}</p>
        <p className="text-body-secondary mb-0">
          <i className="bi bi-geo-alt me-1" aria-hidden="true"></i>{alojamiento.direccion}, {alojamiento.ciudad}
        </p>
      </header>

      <div className="horarios">
        <div>
          <span className="horario-etiqueta">Llegada desde</span>
          <span className="horario-hora">{horaLegible(alojamiento.horaEntrada)}</span>
        </div>
        <div>
          <span className="horario-etiqueta">Salida hasta</span>
          <span className="horario-hora">{horaLegible(alojamiento.horaSalida)}</span>
        </div>
      </div>

      <div className="row g-5">
        <div className="col-12 col-lg-6">
          <h2 className="h3">Antes de reservar</h2>
          <dl className="reglas">
            <dt>Anticipo</dt>
            <dd>
              {alojamiento.exigeAnticipo
                ? `El ${alojamiento.porcentajeAnticipo} % del valor, en recepción, para confirmar la reserva.`
                : 'No se pide anticipo; la recepción confirma la reserva.'}
            </dd>
            <dt>Niños</dt>
            <dd>Los menores de {alojamiento.edadUmbralFacturable} años no pagan noche.</dd>
            <dt>Convivencia</dt>
            <dd>{alojamiento.normasConvivencia}</dd>
          </dl>
        </div>

        <div className="col-12 col-lg-6">
          <h2 className="h3">Servicios adicionales</h2>
          {alojamiento.serviciosAdicionales.length === 0 ? (
            <p className="text-body-secondary">Por ahora no hay servicios adicionales.</p>
          ) : (
            <ul className="carta">
              {alojamiento.serviciosAdicionales.map((servicio) => (
                <li key={servicio.nombre}>
                  <span>{servicio.nombre}</span>
                  <span className="carta-relleno" aria-hidden="true"></span>
                  <strong className="cifra">{formatearPesos(servicio.valor)}</strong>
                </li>
              ))}
            </ul>
          )}
          <p className="small text-body-secondary">Se piden en recepción y se cargan al folio de tu reserva.</p>
        </div>
      </div>

      <div className="mt-5">
        <Link to="/disponibilidad" className="btn btn-primary">Buscar fechas</Link>
      </div>
    </section>
  );
}
