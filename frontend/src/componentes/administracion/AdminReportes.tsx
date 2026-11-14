import { useEffect, useState } from 'react';
import type { Reporte } from '../../modelos/administracion';
import { ETIQUETA_ESTADO, type EstadoReserva } from '../../modelos/reserva';
import { obtenerReporte } from '../../servicios/administracionServicio';
import { mensajeDeError } from '../../utilidades/errores';
import { formatearFecha, formatearPesos } from '../../utilidades/formato';
import { etiquetaCanal } from '../../utilidades/identidad';
import { Cargando } from '../Cargando';

function fechaISO(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

// Por defecto, el mes en curso
function mesActual(): { desde: string; hasta: string } {
  const hoy = new Date();
  return {
    desde: fechaISO(new Date(hoy.getFullYear(), hoy.getMonth(), 1)),
    hasta: fechaISO(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0)),
  };
}

// Reporte de gestión (GET /api/reportes): ocupación, ingresos y reservas de un periodo
export function AdminReportes() {
  const [periodo, setPeriodo] = useState(mesActual);
  const [reporte, setReporte] = useState<Reporte | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function consultar(desde: string, hasta: string) {
    setCargando(true);
    try {
      setReporte(await obtenerReporte(desde, hasta));
      setError(null);
    } catch (e: unknown) {
      setError(mensajeDeError(e));
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    const inicial = mesActual();
    void consultar(inicial.desde, inicial.hasta);
  }, []);

  function enviar(evento: React.FormEvent) {
    evento.preventDefault();
    void consultar(periodo.desde, periodo.hasta);
  }

  return (
    <div>
      <form className="row g-2 align-items-end mb-4" onSubmit={enviar}>
        <div className="col-6 col-md-3">
          <label className="form-label" htmlFor="reporte-desde">Desde</label>
          <input id="reporte-desde" type="date" className="form-control" value={periodo.desde} onChange={(e) => setPeriodo({ ...periodo, desde: e.target.value })} required />
        </div>
        <div className="col-6 col-md-3">
          <label className="form-label" htmlFor="reporte-hasta">Hasta</label>
          <input id="reporte-hasta" type="date" className="form-control" min={periodo.desde} value={periodo.hasta} onChange={(e) => setPeriodo({ ...periodo, hasta: e.target.value })} required />
        </div>
        <div className="col-12 col-md-auto">
          <button className="btn btn-primary" disabled={cargando}>Consultar</button>
        </div>
      </form>

      {cargando && <Cargando />}
      {!cargando && error && <div className="alert alert-danger" role="alert">{error}</div>}

      {!cargando && !error && reporte && (
        <>
          <p className="reporte-resumen">
            Del {formatearFecha(reporte.desde)} al {formatearFecha(reporte.hasta)} se ocuparon{' '}
            <strong className="cifra">{reporte.nochesOcupadas}</strong> de {reporte.nochesDisponibles} noches
            (<strong className="cifra">{reporte.porcentajeOcupacion}&nbsp;%</strong>) y entraron{' '}
            <strong className="cifra">{formatearPesos(reporte.ingresos)}</strong> en pagos.
          </p>

          <div className="row g-4">
            <div className="col-12 col-lg-7">
              <div className="card">
                <div className="card-header"><h2 className="h5 mb-0">Ocupación por apartamento</h2></div>
                <div className="card-body">
                  {reporte.apartamentos.length === 0 && <p className="text-body-secondary mb-0">No hay apartamentos activos.</p>}
                  <ul className="ocupacion">
                    {reporte.apartamentos.map((apartamento) => (
                      <li key={apartamento.identificacion}>
                        <span className="ocupacion-nombre">{apartamento.nombre}</span>
                        <span className="ocupacion-barra" aria-hidden="true"><span style={{ width: `${Math.min(apartamento.porcentajeOcupacion, 100)}%` }}></span></span>
                        <span className="ocupacion-valor cifra">{apartamento.porcentajeOcupacion} %<span className="text-body-secondary"> · {apartamento.nochesOcupadas} {apartamento.nochesOcupadas === 1 ? 'noche' : 'noches'}</span></span>
                      </li>
                    ))}
                  </ul>
                  <p className="small text-body-secondary mb-0 mt-3">Cuentan las noches de reservas confirmadas, en curso y finalizadas.</p>
                </div>
              </div>
            </div>

            <div className="col-12 col-lg-5">
              <div className="card">
                <div className="card-header"><h2 className="h5 mb-0">Reservas con llegada en el periodo</h2></div>
                <div className="card-body">
                  <dl className="ficha mb-3">
                    <div><dt>Reservas</dt><dd className="cifra">{reporte.totalReservas}</dd></div>
                    <div><dt>Valor reservado</dt><dd className="cifra">{formatearPesos(reporte.valorReservado)}</dd></div>
                  </dl>
                  <table className="table table-sm align-middle mb-3">
                    <thead><tr><th>Estado</th><th className="text-end">Reservas</th></tr></thead>
                    <tbody>
                      {reporte.reservasPorEstado.map((fila) => (
                        <tr key={fila.nombre} className={fila.cantidad === 0 ? 'text-body-secondary' : undefined}>
                          <td>{ETIQUETA_ESTADO[fila.nombre as EstadoReserva] ?? fila.nombre}</td>
                          <td className="text-end cifra">{fila.cantidad}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <table className="table table-sm align-middle mb-0">
                    <thead><tr><th>Origen</th><th className="text-end">Reservas</th></tr></thead>
                    <tbody>
                      {reporte.reservasPorCanal.map((fila) => (
                        <tr key={fila.nombre} className={fila.cantidad === 0 ? 'text-body-secondary' : undefined}>
                          <td>{etiquetaCanal(fila.nombre)}</td>
                          <td className="text-end cifra">{fila.cantidad}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
