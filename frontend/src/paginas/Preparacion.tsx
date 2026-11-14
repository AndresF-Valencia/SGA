import { useEffect, useState } from 'react';
import { Cargando } from '../componentes/Cargando';
import type { Novedad } from '../modelos/administracion';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { Puerta } from '../componentes/Puerta';
import { etiquetaEstadoOperativo } from '../utilidades/identidad';
import { cambiarEstadoOperativo, listarApartamentos, listarNovedades, registrarNovedad } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { formatearFecha } from '../utilidades/formato';
import { notificarError, notificarExito } from '../utilidades/notificaciones';
import { useAuth } from '../hooks/useAuth';

const GRAVEDADES: Record<string, string> = { LEVE: 'Leve', MODERADA: 'Moderada', GRAVE: 'Grave' };

export default function Preparacion() {
  const { tieneRol } = useAuth();
  const esAdministrador = tieneRol('ADMINISTRADOR');
  const [apartamentos, setApartamentos] = useState<ApartamentoResumen[]>([]);
  const [cargando, setCargando] = useState(true);
  // Apartamento con el panel de novedades abierto, sus novedades y el formulario del reporte
  const [abierto, setAbierto] = useState<string | null>(null);
  const [novedades, setNovedades] = useState<Novedad[]>([]);
  const [cargandoNovedades, setCargandoNovedades] = useState(false);
  const [reporte, setReporte] = useState({ descripcion: '', gravedad: 'LEVE' });
  const puedeReportar = tieneRol('PERSONAL_SERVICIO', 'ADMINISTRADOR');

  async function cargar() {
    try {
      setApartamentos(await listarApartamentos());
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => { void cargar(); }, []);

  async function cambiar(apartamento: ApartamentoResumen, estado: string) {
    try {
      const actualizado = await cambiarEstadoOperativo(apartamento.identificacion, estado);
      setApartamentos((actuales) => actuales.map((actual) => actual.identificacion === actualizado.identificacion ? actualizado : actual));
      await notificarExito(`${apartamento.identificacion} ahora está ${estado.toLowerCase().replaceAll('_', ' ')}.`);
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function abrirNovedades(identificacion: string) {
    if (abierto === identificacion) {
      setAbierto(null);
      return;
    }
    setAbierto(identificacion);
    setNovedades([]);
    setReporte({ descripcion: '', gravedad: 'LEVE' });
    setCargandoNovedades(true);
    try {
      const pagina = await listarNovedades(identificacion);
      // El backend las entrega de la más antigua a la más nueva
      setNovedades([...pagina.contenido].reverse());
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    } finally {
      setCargandoNovedades(false);
    }
  }

  async function reportar(evento: React.FormEvent, identificacion: string) {
    evento.preventDefault();
    try {
      const nueva = await registrarNovedad(identificacion, { descripcion: reporte.descripcion.trim(), gravedad: reporte.gravedad });
      setNovedades((actuales) => [nueva, ...actuales]);
      setReporte({ descripcion: '', gravedad: 'LEVE' });
      await notificarExito('Novedad reportada.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  if (cargando) return <Cargando />;

  // Tablero por estado, en el orden en que avanza un apartamento después de una salida
  const columnas: { estado: string; ayuda: string }[] = [
    { estado: 'PENDIENTE_PREPARACION', ayuda: 'El grupo ya salió' },
    { estado: 'EN_PREPARACION', ayuda: 'Limpieza en curso' },
    { estado: 'PREPARADO', ayuda: 'Puede recibir huéspedes' },
    { estado: 'OCUPADO', ayuda: 'Con huéspedes' },
    { estado: 'FUERA_DE_SERVICIO', ayuda: 'No se vende' },
  ];
  const activos = apartamentos.filter((apartamento) => apartamento.activo);

  return (
    <section>
      <div className="titulo-pagina">
        <div>
          <h1 className="h2">Preparación de apartamentos</h1>
          <p className="text-body-secondary mb-0">Cuando termines un apartamento, márcalo como listo para que se pueda entregar.</p>
        </div>
      </div>
      <div className="tablero">
        {columnas.map(({ estado, ayuda }) => {
          const enColumna = activos.filter((a) => a.estadoOperativo === estado);
          return (
            <div key={estado} className="tablero-columna">
              <h2 className="h6 mb-0">{etiquetaEstadoOperativo(estado)} <span className="text-body-secondary fw-normal">({enColumna.length})</span></h2>
              <p className="small text-body-secondary mb-2">{ayuda}</p>
              {enColumna.length === 0 && <p className="tablero-vacio small">Ninguno</p>}
              {enColumna.map((apartamento) => (
                <article key={apartamento.identificacion} className="card shadow-sm mb-2">
                  <div className="card-body p-3">
                    <div className="d-flex align-items-center gap-2 mb-2">
                      <Puerta identificacion={apartamento.identificacion} tamano="md" />
                      <div>
                        <h3 className="h5 mb-0">{apartamento.nombre}</h3>
                        <span className="small text-body-secondary">{apartamento.identificacion}</span>
                      </div>
                    </div>
                    <div className="d-flex flex-wrap gap-2">
                      {apartamento.estadoOperativo === 'PENDIENTE_PREPARACION' && (
                        <button type="button" className="btn btn-primary btn-sm" onClick={() => void cambiar(apartamento, 'EN_PREPARACION')}>Empezar preparación</button>
                      )}
                      {apartamento.estadoOperativo === 'EN_PREPARACION' && (
                        <button type="button" className="btn btn-success btn-sm" onClick={() => void cambiar(apartamento, 'PREPARADO')}>Marcar como listo</button>
                      )}
                      {esAdministrador && ['PREPARADO', 'PENDIENTE_PREPARACION', 'EN_PREPARACION'].includes(apartamento.estadoOperativo) && (
                        <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => void cambiar(apartamento, 'FUERA_DE_SERVICIO')}>Sacar de servicio</button>
                      )}
                      {puedeReportar && (
                        <button type="button" className="btn btn-outline-secondary btn-sm" aria-expanded={abierto === apartamento.identificacion} onClick={() => void abrirNovedades(apartamento.identificacion)}>
                          {abierto === apartamento.identificacion ? 'Cerrar novedades' : 'Novedades'}
                        </button>
                      )}
                    </div>
                    {abierto === apartamento.identificacion && (
                      <div className="mt-3">
                        <form onSubmit={(evento) => void reportar(evento, apartamento.identificacion)}>
                          <label className="form-label small mb-1" htmlFor={`novedad-${apartamento.identificacion}`}>Reportar un daño o algo que falte</label>
                          <textarea id={`novedad-${apartamento.identificacion}`} className="form-control form-control-sm mb-2" rows={2} minLength={5} maxLength={500} placeholder="Ej.: la llave del lavamanos gotea" value={reporte.descripcion} onChange={(e) => setReporte({ ...reporte, descripcion: e.target.value })} required />
                          <div className="d-flex gap-2 mb-3">
                            <select className="form-select form-select-sm" aria-label="Gravedad" value={reporte.gravedad} onChange={(e) => setReporte({ ...reporte, gravedad: e.target.value })}>
                              {Object.entries(GRAVEDADES).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
                            </select>
                            <button className="btn btn-primary btn-sm flex-shrink-0">Reportar</button>
                          </div>
                        </form>
                        {cargandoNovedades && <p className="small text-body-secondary mb-0">Cargando novedades…</p>}
                        {!cargandoNovedades && novedades.length === 0 && <p className="small text-body-secondary mb-0">Sin novedades reportadas.</p>}
                        <ul className="novedades small">
                          {novedades.map((novedad, indice) => (
                            <li key={`${novedad.fechaRegistro}-${indice}`}>
                              <strong>{GRAVEDADES[novedad.gravedad] ?? novedad.gravedad}.</strong> {novedad.descripcion}
                              <span className="d-block text-body-secondary">{formatearFecha(novedad.fechaRegistro.slice(0, 10))}, {novedad.reportadoPor}</span>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>
                </article>
              ))}
            </div>
          );
        })}
      </div>
    </section>
  );
}
