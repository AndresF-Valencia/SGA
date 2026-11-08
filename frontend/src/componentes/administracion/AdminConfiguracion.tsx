import { ETIQUETA_CAMPO } from '../../utilidades/identidad';
import { useCallback, useEffect, useState } from 'react';
import { listarConflictos, obtenerParametrosAlojamiento, publicarPolitica, resolverConflicto, configurarParametrosAlojamiento } from '../../servicios/administracionServicio';
import { Paginacion } from '../Paginacion';
import { mensajeDeError } from '../../utilidades/errores';
import { notificarError, notificarExito } from '../../utilidades/notificaciones';

interface ServicioFila { nombre: string; valor: number }
interface TramoFila { diasMinimo: number; diasMaximo: number; porcentajeRetencion: number }

// Parámetros del alojamiento (PUT /api/alojamiento/parametros). Los servicios adicionales se
// editan como filas de nombre y precio, no como JSON.
export function AdminAlojamiento() {
  const [formulario, setFormulario] = useState({ direccion: '', latitud: '', longitud: '', normasConvivencia: '', horaEntrada: '14:00', horaSalida: '11:00', horaLimiteNoShow: '20:00', horasPreparacion: 3, horasPlazoConfirmacion: 1, edadUmbralFacturable: 5, exigeAnticipo: true, porcentajeAnticipo: 50 });
  const [servicios, setServicios] = useState<ServicioFila[]>([]);
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    obtenerParametrosAlojamiento().then((datos) => {
      setFormulario((actual) => ({ ...actual, direccion: datos.direccion, latitud: String(datos.latitud), longitud: String(datos.longitud), normasConvivencia: datos.normasConvivencia, horaEntrada: datos.horaEntrada?.slice(0, 5) ?? '', horaSalida: datos.horaSalida?.slice(0, 5) ?? '', horaLimiteNoShow: datos.horaLimiteNoShow?.slice(0, 5) ?? '', horasPreparacion: datos.minutosPreparacion / 60, horasPlazoConfirmacion: datos.horasPlazoConfirmacion, edadUmbralFacturable: datos.edadUmbralFacturable, exigeAnticipo: datos.exigeAnticipo, porcentajeAnticipo: datos.porcentajeAnticipo }));
      setServicios((datos.serviciosAdicionales ?? []).map((s: ServicioFila) => ({ nombre: s.nombre, valor: s.valor })));
    }).catch((e: unknown) => void notificarError(mensajeDeError(e)));
  }, []);

  function cambiarServicio(indice: number, cambio: Partial<ServicioFila>) {
    setServicios((actuales) => actuales.map((s, i) => (i === indice ? { ...s, ...cambio } : s)));
  }

  async function guardar(evento: React.FormEvent) {
    evento.preventDefault();
    setGuardando(true);
    try {
      await configurarParametrosAlojamiento({
        ...formulario,
        latitud: Number(formulario.latitud),
        longitud: Number(formulario.longitud),
        serviciosAdicionales: servicios.filter((s) => s.nombre.trim()).map((s) => ({ nombre: s.nombre.trim(), valor: s.valor })),
      });
      await notificarExito('Parámetros actualizados.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    } finally {
      setGuardando(false);
    }
  }

  return (
    <form className="card card-body" onSubmit={(evento) => void guardar(evento)}>
      <h2 className="h5 mb-3">Parámetros del alojamiento</h2>

      <div className="row g-3">
        <div className="col-12 col-md-6">
          <label className="form-label" htmlFor="aloj-direccion">Dirección</label>
          <input id="aloj-direccion" className="form-control" value={formulario.direccion} onChange={(e) => setFormulario({ ...formulario, direccion: e.target.value })} required />
        </div>
        <div className="col-6 col-md-3">
          <label className="form-label" htmlFor="aloj-latitud">Latitud</label>
          <input id="aloj-latitud" className="form-control" inputMode="decimal" value={formulario.latitud} onChange={(e) => setFormulario({ ...formulario, latitud: e.target.value })} required />
        </div>
        <div className="col-6 col-md-3">
          <label className="form-label" htmlFor="aloj-longitud">Longitud</label>
          <input id="aloj-longitud" className="form-control" inputMode="decimal" value={formulario.longitud} onChange={(e) => setFormulario({ ...formulario, longitud: e.target.value })} required />
        </div>
        <div className="col-12">
          <label className="form-label" htmlFor="aloj-normas">Normas de convivencia</label>
          <textarea id="aloj-normas" className="form-control" rows={3} value={formulario.normasConvivencia} onChange={(e) => setFormulario({ ...formulario, normasConvivencia: e.target.value })} />
        </div>
        {(['horaEntrada', 'horaSalida', 'horaLimiteNoShow'] as const).map((campo) => (
          <div key={campo} className="col-12 col-md-4">
            <label className="form-label" htmlFor={`aloj-${campo}`}>{ETIQUETA_CAMPO[campo] ?? campo}</label>
            <input id={`aloj-${campo}`} type="time" className="form-control" value={formulario[campo]} onChange={(e) => setFormulario({ ...formulario, [campo]: e.target.value })} />
          </div>
        ))}
        {(['horasPreparacion', 'horasPlazoConfirmacion', 'edadUmbralFacturable', 'porcentajeAnticipo'] as const).map((campo) => (
          <div key={campo} className="col-6 col-md-3">
            <label className="form-label" htmlFor={`aloj-${campo}`}>{ETIQUETA_CAMPO[campo] ?? campo}</label>
            <input id={`aloj-${campo}`} type="number" min="0" max={campo === 'porcentajeAnticipo' ? 100 : undefined} className="form-control" value={formulario[campo]}
              disabled={campo === 'porcentajeAnticipo' && !formulario.exigeAnticipo}
              onChange={(e) => setFormulario({ ...formulario, [campo]: Number(e.target.value) })} />
          </div>
        ))}
        <div className="col-12">
          <div className="form-check">
            <input id="aloj-anticipo" type="checkbox" className="form-check-input" checked={formulario.exigeAnticipo} onChange={(e) => setFormulario({ ...formulario, exigeAnticipo: e.target.checked })} />
            <label className="form-check-label" htmlFor="aloj-anticipo">Exigir anticipo para confirmar las reservas</label>
          </div>
        </div>
      </div>

      <fieldset className="mt-4">
        <legend className="h6 mb-1">Servicios adicionales</legend>
        <p className="small text-body-secondary">Se muestran en la página del alojamiento y la recepción los carga al folio.</p>
        {servicios.length === 0 && <p className="small text-body-secondary fst-italic">Sin servicios. Agrega el primero.</p>}
        {servicios.map((servicio, indice) => (
          <div key={indice} className="d-flex flex-wrap gap-2 align-items-end mb-2">
            <div className="flex-grow-1" style={{ minWidth: '12rem' }}>
              <label className="form-label small mb-1" htmlFor={`serv-nombre-${indice}`}>Servicio</label>
              <input id={`serv-nombre-${indice}`} className="form-control" value={servicio.nombre} placeholder="Desayuno" onChange={(e) => cambiarServicio(indice, { nombre: e.target.value })} required />
            </div>
            <div style={{ width: '10rem' }}>
              <label className="form-label small mb-1" htmlFor={`serv-valor-${indice}`}>Precio (COP)</label>
              <input id={`serv-valor-${indice}`} type="number" min="0" step="1000" className="form-control" value={servicio.valor} onChange={(e) => cambiarServicio(indice, { valor: Number(e.target.value) })} required />
            </div>
            <button type="button" className="btn btn-outline-danger" aria-label={`Quitar ${servicio.nombre || 'servicio'}`} onClick={() => setServicios(servicios.filter((_, i) => i !== indice))}>
              <i className="bi bi-x-lg" aria-hidden="true"></i>
            </button>
          </div>
        ))}
        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={() => setServicios([...servicios, { nombre: '', valor: 0 }])}>
          <i className="bi bi-plus-lg me-1" aria-hidden="true"></i>Agregar servicio
        </button>
      </fieldset>

      <button className="btn btn-primary align-self-start mt-4" disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar parámetros'}</button>
    </form>
  );
}

// Errores de los tramos antes de enviar (el backend vuelve a validar las reglas de negocio)
function revisarTramos(tramos: TramoFila[]): string | null {
  for (const [i, t] of tramos.entries()) {
    if (t.diasMaximo < t.diasMinimo) return `Tramo ${i + 1}: "hasta" debe ser mayor o igual que "desde".`;
    if (t.porcentajeRetencion < 0 || t.porcentajeRetencion > 100) return `Tramo ${i + 1}: el porcentaje va de 0 a 100.`;
  }
  return null;
}

// Política de cancelación (POST /api/politicas-cancelacion): una versión nueva con sus tramos.
// Cada tramo dice cuánto se retiene si el huésped cancela con esos días de anticipación.
export function AdminPolitica() {
  const [version, setVersion] = useState('2');
  const [tramos, setTramos] = useState<TramoFila[]>([
    { diasMinimo: 0, diasMaximo: 6, porcentajeRetencion: 100 },
    { diasMinimo: 7, diasMaximo: 9999, porcentajeRetencion: 0 },
  ]);
  const [aviso, setAviso] = useState<string | null>(null);
  const [publicando, setPublicando] = useState(false);

  function cambiarTramo(indice: number, cambio: Partial<TramoFila>) {
    setTramos((actuales) => actuales.map((t, i) => (i === indice ? { ...t, ...cambio } : t)));
  }

  function agregarTramo() {
    const ultimo = tramos[tramos.length - 1];
    const desde = ultimo ? Math.min(ultimo.diasMaximo + 1, 9999) : 0;
    setTramos([...tramos, { diasMinimo: desde, diasMaximo: 9999, porcentajeRetencion: 0 }]);
  }

  async function publicar(evento: React.FormEvent) {
    evento.preventDefault();
    const problema = revisarTramos(tramos);
    setAviso(problema);
    if (problema) return;
    setPublicando(true);
    try {
      await publicarPolitica({ version: version.trim(), tramos });
      await notificarExito(`Política versión ${version.trim()} publicada.`);
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    } finally {
      setPublicando(false);
    }
  }

  return (
    <form className="card card-body" onSubmit={(evento) => void publicar(evento)}>
      <h2 className="h5 mb-1">Publicar política de cancelación</h2>
      <p className="small text-body-secondary mb-3">
        Cada tramo dice cuánto del valor se queda el alojamiento si el huésped cancela con esos días de anticipación.
        Las versiones anteriores se conservan para las reservas que ya las tenían.
      </p>

      <div className="mb-3" style={{ maxWidth: '12rem' }}>
        <label className="form-label" htmlFor="politica-version">Versión</label>
        <input id="politica-version" className="form-control" value={version} onChange={(e) => setVersion(e.target.value)} required />
      </div>

      <div className="table-responsive">
        <table className="table align-middle mb-2">
          <thead>
            <tr><th>Si cancela desde (días antes)</th><th>hasta (días antes)</th><th>Se retiene (%)</th><th><span className="visually-hidden">Quitar</span></th></tr>
          </thead>
          <tbody>
            {tramos.map((tramo, indice) => (
              <tr key={indice}>
                <td><input aria-label={`Tramo ${indice + 1}, desde`} type="number" min="0" className="form-control" value={tramo.diasMinimo} onChange={(e) => cambiarTramo(indice, { diasMinimo: Number(e.target.value) })} required /></td>
                <td><input aria-label={`Tramo ${indice + 1}, hasta`} type="number" min="0" className="form-control" value={tramo.diasMaximo} onChange={(e) => cambiarTramo(indice, { diasMaximo: Number(e.target.value) })} required /></td>
                <td><input aria-label={`Tramo ${indice + 1}, porcentaje retenido`} type="number" min="0" max="100" className="form-control" value={tramo.porcentajeRetencion} onChange={(e) => cambiarTramo(indice, { porcentajeRetencion: Number(e.target.value) })} required /></td>
                <td className="text-end">
                  {tramos.length > 1 && (
                    <button type="button" className="btn btn-outline-danger" aria-label={`Quitar tramo ${indice + 1}`} onClick={() => setTramos(tramos.filter((_, i) => i !== indice))}>
                      <i className="bi bi-x-lg" aria-hidden="true"></i>
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="small text-body-secondary">Usa 9999 en "hasta" para decir "o más días".</p>

      <button type="button" className="btn btn-outline-secondary btn-sm align-self-start" onClick={agregarTramo}>
        <i className="bi bi-plus-lg me-1" aria-hidden="true"></i>Agregar tramo
      </button>

      {aviso && <div className="alert alert-warning mt-3 mb-0">{aviso}</div>}

      <button className="btn btn-primary align-self-start mt-4" disabled={publicando}>{publicando ? 'Publicando…' : 'Publicar política'}</button>
    </form>
  );
}

export function AdminConflictos() {
  const [conflictos, setConflictos] = useState<{ id: string; canal: string; codigoExterno: string; motivo: string; estado: string }[]>([]);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [seleccionado, setSeleccionado] = useState<string | null>(null);
  const [resolucion, setResolucion] = useState('');
  const cargar = useCallback(async () => { try { const datos = await listarConflictos(pagina); setConflictos(datos.contenido); setTotalPaginas(datos.totalPaginas); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }, [pagina]);
  useEffect(() => { void cargar(); }, [cargar]);
  async function resolver(evento: React.FormEvent) { evento.preventDefault(); if (!seleccionado || !resolucion.trim()) return; try { await resolverConflicto(seleccionado, resolucion.trim()); setSeleccionado(null); setResolucion(''); await notificarExito('Conflicto resuelto.'); await cargar(); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  return <><div className="table-responsive"><table className="table"><thead><tr><th>Canal</th><th>Código externo</th><th>Motivo</th><th>Estado</th><th /></tr></thead><tbody>{conflictos.map((conflicto) => <tr key={conflicto.id}><td>{conflicto.canal}</td><td>{conflicto.codigoExterno}</td><td>{conflicto.motivo}</td><td>{conflicto.estado === 'RESUELTO' ? 'Resuelto' : 'Pendiente'}</td><td>{conflicto.estado !== 'RESUELTO' && <button type="button" className="btn btn-sm btn-outline-primary" onClick={() => setSeleccionado(conflicto.id)}>Resolver</button>}</td></tr>)}</tbody></table></div><Paginacion paginaActual={pagina} totalPaginas={totalPaginas} alCambiar={setPagina} />{seleccionado && <form className="card card-body mt-3" onSubmit={(evento) => void resolver(evento)}><label className="form-label">Resolución<textarea className="form-control" minLength={5} maxLength={300} value={resolucion} onChange={(e) => setResolucion(e.target.value)} required /></label><div className="d-flex gap-2"><button className="btn btn-primary">Guardar resolución</button><button type="button" className="btn btn-outline-secondary" onClick={() => setSeleccionado(null)}>Cancelar</button></div></form>}</>;
}
