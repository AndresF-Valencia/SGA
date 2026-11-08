import { useEffect, useState } from 'react';
import { Cargando } from '../componentes/Cargando';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { Puerta } from '../componentes/Puerta';
import { etiquetaEstadoOperativo } from '../utilidades/identidad';
import { cambiarEstadoOperativo, listarApartamentos } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { notificarError, notificarExito } from '../utilidades/notificaciones';
import { useAuth } from '../hooks/useAuth';

export default function Preparacion() {
  const { tieneRol } = useAuth();
  const esAdministrador = tieneRol('ADMINISTRADOR');
  const [apartamentos, setApartamentos] = useState<ApartamentoResumen[]>([]);
  const [cargando, setCargando] = useState(true);

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
                    </div>
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
