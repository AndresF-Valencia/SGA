import { useCallback, useEffect, useState } from 'react';
import type { Bloqueo } from '../../modelos/administracion';
import type { ApartamentoResumen } from '../../modelos/apartamento';
import { crearBloqueo, levantarBloqueo, listarBloqueos } from '../../servicios/administracionServicio';
import { listarApartamentos } from '../../servicios/apartamentoServicio';
import { mensajeDeError } from '../../utilidades/errores';
import { formatearFecha } from '../../utilidades/formato';
import { ETIQUETA_MOTIVO_BLOQUEO } from '../../utilidades/identidad';
import { confirmarAccion, notificarError, notificarExito } from '../../utilidades/notificaciones';
import { Paginacion } from '../Paginacion';

const INICIAL = { identificacionApartamento: '', fechaInicio: '', fechaFin: '', motivo: '' };

// Bloqueos administrativos (RN-07): quitan un apartamento de la venta durante un periodo.
// No se borran: se levantan, y el historial queda en la lista.
export function AdminBloqueos() {
  const [formulario, setFormulario] = useState(INICIAL);
  const [apartamentos, setApartamentos] = useState<ApartamentoResumen[]>([]);
  const [bloqueos, setBloqueos] = useState<Bloqueo[]>([]);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);

  const cargar = useCallback(async (numero: number) => {
    try {
      const datos = await listarBloqueos(numero);
      setBloqueos(datos.contenido);
      setTotalPaginas(datos.totalPaginas);
      setPagina(numero);
      setError(null);
    } catch (e: unknown) {
      setError(mensajeDeError(e));
    }
  }, []);

  useEffect(() => {
    void cargar(0);
    listarApartamentos().then(setApartamentos).catch(() => setApartamentos([]));
  }, [cargar]);

  async function crear(evento: React.FormEvent) {
    evento.preventDefault();
    setGuardando(true);
    try {
      await crearBloqueo(formulario);
      setFormulario(INICIAL);
      await cargar(0);
      await notificarExito('Bloqueo registrado.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    } finally {
      setGuardando(false);
    }
  }

  async function levantar(bloqueo: Bloqueo) {
    if (!await confirmarAccion('¿Levantar este bloqueo?', `${bloqueo.identificacionApartamento} vuelve a estar disponible en esas fechas.`)) return;
    try {
      await levantarBloqueo(bloqueo.id);
      await cargar(pagina);
      await notificarExito('Bloqueo levantado.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  return (
    <div className="row g-4">
      <div className="col-12 col-lg-4">
        <form className="card card-body" onSubmit={(evento) => void crear(evento)}>
          <h2 className="h5">Registrar bloqueo</h2>
          <p className="small text-body-secondary">El apartamento no se puede reservar en esas fechas. No se permite si ya hay reservas activas.</p>
          <div className="mb-2">
            <label className="form-label" htmlFor="bloqueo-apto">Apartamento</label>
            <select id="bloqueo-apto" className="form-select" value={formulario.identificacionApartamento} onChange={(e) => setFormulario({ ...formulario, identificacionApartamento: e.target.value })} required>
              <option value="">Elige…</option>
              {apartamentos.map((a) => <option key={a.identificacion} value={a.identificacion}>{a.nombre} ({a.identificacion})</option>)}
            </select>
          </div>
          <div className="row g-2 mb-2">
            <div className="col-6">
              <label className="form-label" htmlFor="bloqueo-desde">Desde</label>
              <input id="bloqueo-desde" type="date" className="form-control" value={formulario.fechaInicio} onChange={(e) => setFormulario({ ...formulario, fechaInicio: e.target.value })} required />
            </div>
            <div className="col-6">
              <label className="form-label" htmlFor="bloqueo-hasta">Hasta</label>
              <input id="bloqueo-hasta" type="date" className="form-control" min={formulario.fechaInicio || undefined} value={formulario.fechaFin} onChange={(e) => setFormulario({ ...formulario, fechaFin: e.target.value })} required />
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label" htmlFor="bloqueo-motivo">Motivo</label>
            <select id="bloqueo-motivo" className="form-select" value={formulario.motivo} onChange={(e) => setFormulario({ ...formulario, motivo: e.target.value })} required>
              <option value="">Elige…</option>
              {Object.entries(ETIQUETA_MOTIVO_BLOQUEO).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
            </select>
          </div>
          <button className="btn btn-primary align-self-start" disabled={guardando}>{guardando ? 'Registrando…' : 'Registrar bloqueo'}</button>
        </form>
      </div>

      <div className="col-12 col-lg-8">
        <div className="card">
          <div className="card-header"><h2 className="h5 mb-0">Bloqueos registrados</h2></div>
          <div className="card-body">
            {error && <div className="alert alert-danger" role="alert">{error}</div>}
            {!error && bloqueos.length === 0 && <p className="text-body-secondary mb-0">Todavía no hay bloqueos. Cuando registres uno, aparece aquí.</p>}
            {bloqueos.length > 0 && (
              <div className="table-responsive">
                <table className="table align-middle mb-0">
                  <thead><tr><th>Apartamento</th><th>Periodo</th><th>Motivo</th><th>Estado</th><th><span className="visually-hidden">Acciones</span></th></tr></thead>
                  <tbody>
                    {bloqueos.map((bloqueo) => (
                      <tr key={bloqueo.id}>
                        <td>{bloqueo.identificacionApartamento}</td>
                        <td>{formatearFecha(bloqueo.fechaInicio)} al {formatearFecha(bloqueo.fechaFin)}</td>
                        <td>{ETIQUETA_MOTIVO_BLOQUEO[bloqueo.motivo] ?? bloqueo.motivo}</td>
                        <td><span className={`badge ${bloqueo.estado === 'VIGENTE' ? 'text-bg-warning' : 'text-bg-secondary'}`}>{bloqueo.estado === 'VIGENTE' ? 'Vigente' : 'Levantado'}</span></td>
                        <td className="text-end">
                          {bloqueo.estado === 'VIGENTE' && <button type="button" className="btn btn-outline-secondary btn-sm" onClick={() => void levantar(bloqueo)}>Levantar</button>}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            <Paginacion paginaActual={pagina} totalPaginas={totalPaginas} alCambiar={(numero) => void cargar(numero)} />
          </div>
        </div>
      </div>
    </div>
  );
}
