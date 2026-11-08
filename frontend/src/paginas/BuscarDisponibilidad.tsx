// Página "/disponibilidad" (pública, 7.5): valida con esquemaDisponibilidad y consulta
// disponibilidadServicio (GET /api/disponibilidad). Resultados en tarjetas con formatearPesos.
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { Campo } from '../componentes/Campo';
import { Cargando } from '../componentes/Cargando';
import { esquemaDisponibilidad, type FormularioDisponibilidad } from '../esquemas/disponibilidad';
import type { DisponibilidadResponse } from '../modelos/disponibilidad';
import { buscarDisponibilidad } from '../servicios/disponibilidadServicio';
import { mensajeDeError } from '../utilidades/errores';
import { formatearFecha, formatearPesos } from '../utilidades/formato';

// Formulario de búsqueda y grid de apartamentos disponibles en todas las noches del rango
export default function BuscarDisponibilidad() {
  const [resultado, setResultado] = useState<DisponibilidadResponse | null>(null);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioDisponibilidad>({
    resolver: zodResolver(esquemaDisponibilidad),
    mode: 'onTouched',
    defaultValues: { fechaEntrada: '', fechaSalida: '', personas: 2, mascotas: 0 },
  });

  // El formulario ya tiene la forma de los parámetros de la consulta
  async function alEnviar(datos: FormularioDisponibilidad) {
    setResultado(null);
    try {
      setResultado(await buscarDisponibilidad(datos));
    } catch (error) {
      setError('root.servidor', { message: mensajeDeError(error) });
    }
  }

  return (
    <section>
      <h1 className="h3 mb-3"><i className="bi bi-search me-2" aria-hidden="true"></i>Buscar disponibilidad</h1>

      <form onSubmit={handleSubmit(alEnviar)} noValidate className="card card-body mb-4">
        <div className="row align-items-end">
          <div className="col-12 col-md-3">
            <Campo id="fechaEntrada" etiqueta="Entrada" type="date"
              error={errors.fechaEntrada?.message} {...register('fechaEntrada')} />
          </div>
          <div className="col-12 col-md-3">
            <Campo id="fechaSalida" etiqueta="Salida" type="date"
              error={errors.fechaSalida?.message} {...register('fechaSalida')} />
          </div>
          <div className="col-6 col-md-2">
            <Campo id="personas" etiqueta="Personas" type="number" min={1}
              error={errors.personas?.message} {...register('personas', { valueAsNumber: true })} />
          </div>
          <div className="col-6 col-md-2">
            <Campo id="mascotas" etiqueta="Mascotas" type="number" min={0}
              error={errors.mascotas?.message} {...register('mascotas', { valueAsNumber: true })} />
          </div>
          <div className="col-12 col-md-2 mb-3 d-grid">
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              <i className="bi bi-search me-1" aria-hidden="true"></i>Buscar
            </button>
          </div>
        </div>
        {errors.root?.servidor && <div className="alert alert-danger mb-0">{errors.root.servidor.message}</div>}
      </form>

      {isSubmitting && <Cargando />}

      {resultado && resultado.totalDisponibles === 0 && (
        <div className="text-center text-body-secondary py-5">
          <i className="bi bi-calendar-x fs-1 d-block mb-2" aria-hidden="true"></i>
          No hay apartamentos disponibles para esas fechas.
        </div>
      )}

      {resultado && resultado.totalDisponibles > 0 && (
        <>
          <p className="text-body-secondary">
            {resultado.totalDisponibles} apartamento(s) disponibles del {formatearFecha(resultado.fechaEntrada)} al{' '}
            {formatearFecha(resultado.fechaSalida)}.
          </p>
          <div className="row g-3">
            {resultado.apartamentosDisponibles.map((apto) => (
              <div key={apto.identificacionApartamento} className="col-12 col-md-6 col-lg-4">
                <article className="card h-100 shadow-sm">
                  <div className="card-body">
                    <h2 className="h5 card-title">{apto.nombre}</h2>
                    <p className="text-body-secondary small mb-2">
                      {apto.identificacionApartamento}
                      <span className="mx-2">·</span>
                      <i className="bi bi-people me-1" aria-hidden="true"></i>hasta {apto.capacidadMaxima} personas
                    </p>
                    <p className="mb-0">
                      {apto.tarifaEstimadaPorNoche !== null
                        ? <><strong>{formatearPesos(apto.tarifaEstimadaPorNoche)}</strong> <small className="text-body-secondary">por noche (estimado)</small></>
                        : <small className="text-body-secondary">Sin tarifa estimada</small>}
                    </p>
                  </div>
                  <div className="card-footer bg-transparent d-flex gap-2">
                    <Link to={`/apartamentos/${apto.identificacionApartamento}`} className="btn btn-outline-primary btn-sm">
                      Ver detalle
                    </Link>
                    <Link to="/cotizacion" className="btn btn-outline-secondary btn-sm">Cotizar</Link>
                  </div>
                </article>
              </div>
            ))}
          </div>
        </>
      )}
    </section>
  );
}
