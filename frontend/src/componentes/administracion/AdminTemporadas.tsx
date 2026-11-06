import { useCallback, useEffect, useState } from 'react';
import type { Temporada } from '../../modelos/administracion';
import { configurarTarifa, crearTemporada, listarTodasLasTemporadas, listarTemporadas } from '../../servicios/administracionServicio';
import { Paginacion } from '../Paginacion';
import { mensajeDeError } from '../../utilidades/errores';
import { notificarError, notificarExito } from '../../utilidades/notificaciones';

const INICIAL = { nombre: '', fechaInicio: '', fechaFin: '', estanciaMinima: 1, tipo: 'BASE' };

export function AdminTemporadas() {
  const [temporadas, setTemporadas] = useState<Temporada[]>([]);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [formulario, setFormulario] = useState(INICIAL);
  const [tarifa, setTarifa] = useState({ identificacion: '', temporadaId: '', valorTarifa: 0 });
  const [cargandoTarifas, setCargandoTarifas] = useState(false);

  const cargar = useCallback(async () => {
    try {
      const datos = await listarTemporadas(pagina);
      setTemporadas(datos.contenido);
      setTotalPaginas(datos.totalPaginas);
      if (!datos.contenido.some((temporada) => temporada.id === tarifa.temporadaId)) {
        setTarifa((actual) => ({ ...actual, temporadaId: datos.contenido[0]?.id ?? '' }));
      }
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }, [pagina, tarifa.temporadaId]);

  useEffect(() => { void cargar(); }, [cargar]);

  const [asignacionRapida, setAsignacionRapida] = useState({ identificacion: '', tipo: 'BASE', valorTarifa: 0 });

  async function crear(evento: React.FormEvent) {
    evento.preventDefault();
    try {
      await crearTemporada(formulario);
      setFormulario(INICIAL);
      await notificarExito('Temporada creada.');
      await cargar();
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function guardarTarifa(evento: React.FormEvent) {
    evento.preventDefault();
    try {
      await configurarTarifa(tarifa.identificacion, tarifa.temporadaId, tarifa.valorTarifa);
      await notificarExito('Tarifa configurada para la temporada seleccionada.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function guardarTodasLasTarifas(evento: React.FormEvent) {
    evento.preventDefault();
    const temporadas = await listarTodasLasTemporadas();
    const temporadasDelTipo = temporadas.filter((temporada) => temporada.tipoTemporada === asignacionRapida.tipo);
    if (!asignacionRapida.identificacion.trim() || asignacionRapida.valorTarifa < 1) {
      await notificarError('Ingrese el apartamento y un valor válido.');
      return;
    }
    if (temporadasDelTipo.length === 0) {
      await notificarError(`No existen temporadas de tipo ${asignacionRapida.tipo}.`);
      return;
    }
    setCargandoTarifas(true);
    try {
      for (const temporada of temporadasDelTipo) {
        await configurarTarifa(asignacionRapida.identificacion.trim(), temporada.id, asignacionRapida.valorTarifa);
      }
      await notificarExito(`Se configuraron ${temporadasDelTipo.length} tarifas ${asignacionRapida.tipo.toLowerCase()}.`);
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    } finally {
      setCargandoTarifas(false);
    }
  }

  return (
    <div className="row g-4">
      <div className="col-12 col-lg-5">
        <form className="card card-body mb-3" onSubmit={(evento) => void crear(evento)}>
          <h2 className="h5">Crear temporada</h2>
          <input className="form-control mb-2" placeholder="Nombre" value={formulario.nombre} onChange={(e) => setFormulario({ ...formulario, nombre: e.target.value })} required />
          <div className="row">
            <input type="date" className="form-control col m-1" value={formulario.fechaInicio} onChange={(e) => setFormulario({ ...formulario, fechaInicio: e.target.value })} required />
            <input type="date" className="form-control col m-1" value={formulario.fechaFin} onChange={(e) => setFormulario({ ...formulario, fechaFin: e.target.value })} required />
          </div>
          <input type="number" min="1" className="form-control my-2" value={formulario.estanciaMinima} onChange={(e) => setFormulario({ ...formulario, estanciaMinima: Number(e.target.value) })} required />
          <select className="form-select mb-2" value={formulario.tipo} onChange={(e) => setFormulario({ ...formulario, tipo: e.target.value })}><option>BASE</option><option>MEDIA</option><option>ALTA</option></select>
          <button className="btn btn-primary">Crear temporada</button>
        </form>

        <form className="card card-body mb-3" onSubmit={(evento) => void guardarTodasLasTarifas(evento)}>
          <h2 className="h5">Asignación rápida: todas las temporadas</h2>
          <input className="form-control mb-2" placeholder="Apartamento AP001" value={asignacionRapida.identificacion} onChange={(e) => setAsignacionRapida({ ...asignacionRapida, identificacion: e.target.value })} required />
          <label className="form-label">Tipo de temporada<select className="form-select" value={asignacionRapida.tipo} onChange={(e) => setAsignacionRapida({ ...asignacionRapida, tipo: e.target.value })}><option value="BASE">Base</option><option value="MEDIA">Media</option><option value="ALTA">Alta</option></select></label>
          <label className="form-label">Valor por persona y noche<input type="number" min="1" className="form-control" value={asignacionRapida.valorTarifa || ''} onChange={(e) => setAsignacionRapida({ ...asignacionRapida, valorTarifa: Number(e.target.value) })} required /></label>
          <button className="btn btn-primary" disabled={cargandoTarifas}>{cargandoTarifas ? 'Guardando tarifas…' : 'Guardar todas las tarifas'}</button>
        </form>

        <form className="card card-body" onSubmit={(evento) => void guardarTarifa(evento)}>
          <h2 className="h5">Asignación específica</h2>
          <input className="form-control mb-2" placeholder="Apartamento AP001" value={tarifa.identificacion} onChange={(e) => setTarifa({ ...tarifa, identificacion: e.target.value })} required />
          <label className="form-label">Temporada existente
            <select className="form-select" value={tarifa.temporadaId} onChange={(e) => setTarifa({ ...tarifa, temporadaId: e.target.value })} required>
              <option value="">Seleccione una temporada…</option>
              {temporadas.map((temporada) => <option key={temporada.id} value={temporada.id}>{temporada.nombre} · {temporada.fechaInicio} a {temporada.fechaFin}</option>)}
            </select>
          </label>
          <input type="number" min="1" className="form-control mb-2" placeholder="Valor por persona y noche" value={tarifa.valorTarifa || ''} onChange={(e) => setTarifa({ ...tarifa, valorTarifa: Number(e.target.value) })} required />
          <button className="btn btn-outline-primary" disabled={temporadas.length === 0}>Guardar tarifa</button>
        </form>
      </div>

      <div className="col-12 col-lg-7">
        <table className="table"><thead><tr><th>Nombre</th><th>Periodo</th><th>Tipo</th><th>ID</th></tr></thead><tbody>
          {temporadas.map((temporada) => <tr key={temporada.id}><td>{temporada.nombre}</td><td>{temporada.fechaInicio} a {temporada.fechaFin}</td><td>{temporada.tipoTemporada}</td><td><code>{temporada.id}</code></td></tr>)}
        </tbody></table>
        <Paginacion paginaActual={pagina} totalPaginas={totalPaginas} alCambiar={setPagina} />
      </div>
    </div>
  );
}
