import { useState } from 'react';
import { AdminApartamentos } from '../componentes/administracion/AdminApartamentos';
import { AdminAlojamiento, AdminConflictos, AdminPolitica } from '../componentes/administracion/AdminConfiguracion';
import { AdminBloqueos } from '../componentes/administracion/AdminBloqueos';
import { AdminTemporadas } from '../componentes/administracion/AdminTemporadas';
import { AdminUsuarios } from '../componentes/administracion/AdminUsuarios';

type Seccion = 'usuarios' | 'apartamentos' | 'temporadas' | 'bloqueos' | 'alojamiento' | 'politica' | 'conflictos';

const SECCIONES: { id: Seccion; titulo: string }[] = [
  { id: 'usuarios', titulo: 'Usuarios' },
  { id: 'apartamentos', titulo: 'Apartamentos' },
  { id: 'temporadas', titulo: 'Temporadas y tarifas' },
  { id: 'bloqueos', titulo: 'Bloqueos' },
  { id: 'alojamiento', titulo: 'Alojamiento' },
  { id: 'politica', titulo: 'Política' },
  { id: 'conflictos', titulo: 'Conflictos' },
];

export default function Administracion() {
  const [seccion, setSeccion] = useState<Seccion>('usuarios');
  return <section>
    <h1 className="h3 mb-4">Administración del alojamiento</h1>
    <ul className="nav nav-tabs mb-4">{SECCIONES.map((item) => <li className="nav-item" key={item.id}><button type="button" className={`nav-link ${seccion === item.id ? 'active' : ''}`} onClick={() => setSeccion(item.id)}>{item.titulo}</button></li>)}</ul>
    {seccion === 'usuarios' && <AdminUsuarios />}
    {seccion === 'apartamentos' && <AdminApartamentos />}
    {seccion === 'temporadas' && <AdminTemporadas />}
    {seccion === 'bloqueos' && <AdminBloqueos />}
    {seccion === 'alojamiento' && <AdminAlojamiento />}
    {seccion === 'politica' && <AdminPolitica />}
    {seccion === 'conflictos' && <AdminConflictos />}
  </section>;
}
