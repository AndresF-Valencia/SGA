import { ETIQUETA_MOTIVO_BLOQUEO } from '../../utilidades/identidad';
import { useState } from 'react';
import { crearBloqueo } from '../../servicios/administracionServicio';
import { mensajeDeError } from '../../utilidades/errores';
import { notificarError, notificarExito } from '../../utilidades/notificaciones';

const INICIAL = { identificacionApartamento: '', fechaInicio: '', fechaFin: '', motivo: '' };

export function AdminBloqueos() {
  const [formulario, setFormulario] = useState(INICIAL);
  async function crear(evento: React.FormEvent) { evento.preventDefault(); try { await crearBloqueo(formulario); setFormulario(INICIAL); await notificarExito('Bloqueo registrado.'); } catch (e: unknown) { await notificarError(mensajeDeError(e)); } }
  return <form className="card card-body" onSubmit={(evento) => void crear(evento)}><h2 className="h5">Registrar bloqueo</h2><input className="form-control mb-2" placeholder="Apartamento, por ejemplo AP003" value={formulario.identificacionApartamento} onChange={(e) => setFormulario({ ...formulario, identificacionApartamento: e.target.value })} required /><div className="row"><input type="date" className="form-control col m-1" value={formulario.fechaInicio} onChange={(e) => setFormulario({ ...formulario, fechaInicio: e.target.value })} required /><input type="date" className="form-control col m-1" value={formulario.fechaFin} onChange={(e) => setFormulario({ ...formulario, fechaFin: e.target.value })} required /></div><label className="form-label mt-2">Motivo<select className="form-select" value={formulario.motivo} onChange={(e) => setFormulario({ ...formulario, motivo: e.target.value })} required><option value="">Seleccione…</option>{Object.entries(ETIQUETA_MOTIVO_BLOQUEO).map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}</select></label><button className="btn btn-primary align-self-start">Registrar bloqueo</button></form>;
}
