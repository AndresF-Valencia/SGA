import { useEffect, useState } from 'react';
import { Cargando } from '../componentes/Cargando';
import type { ApartamentoResumen } from '../modelos/apartamento';
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

  return (
    <section>
      <h1 className="h3">Preparación de apartamentos</h1>
      <p className="text-body-secondary">
        Cambie el estado operativo respetando las transiciones del alojamiento.
      </p>
      <div className="row g-3">
        {apartamentos.filter((apartamento) => apartamento.activo).map((apartamento) => (
          <article key={apartamento.identificacion} className="col-12 col-md-6 col-lg-4">
            <div className="card h-100">
              <div className="card-body">
                <h2 className="h5">{apartamento.identificacion} · {apartamento.nombre}</h2>
                <p className="mb-3">Estado: <strong>{apartamento.estadoOperativo}</strong></p>
                <div className="d-flex flex-wrap gap-2">
                  {apartamento.estadoOperativo === 'PENDIENTE_PREPARACION' && (
                    <button type="button" className="btn btn-primary btn-sm" onClick={() => void cambiar(apartamento, 'EN_PREPARACION')}>Iniciar preparación</button>
                  )}
                  {apartamento.estadoOperativo === 'EN_PREPARACION' && (
                    <button type="button" className="btn btn-success btn-sm" onClick={() => void cambiar(apartamento, 'PREPARADO')}>Marcar preparado</button>
                  )}
                  {esAdministrador && ['PREPARADO', 'PENDIENTE_PREPARACION', 'EN_PREPARACION'].includes(apartamento.estadoOperativo) && (
                    <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => void cambiar(apartamento, 'FUERA_DE_SERVICIO')}>Fuera de servicio</button>
                  )}
                </div>
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
