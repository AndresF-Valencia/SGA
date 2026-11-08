// Página "/cotizacion" (pública, 7.4): valida con esquemaCotizacion y llama a
// cotizacionServicio (POST /api/cotizaciones). Muestra el total y el desglose por noche.
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useFieldArray, useForm, type Path } from 'react-hook-form';
import { Campo } from '../componentes/Campo';
import { esquemaCotizacion, type FormularioCotizacion } from '../esquemas/cotizacion';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { escucharCambiosApartamentos, listarApartamentos } from '../servicios/apartamentoServicio';
import { escucharCambiosTarifas } from '../servicios/administracionServicio';
import type { CotizacionResponse } from '../modelos/cotizacion';
import { cotizar } from '../servicios/cotizacionServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';
import { formatearFecha, formatearPesos } from '../utilidades/formato';

// Formulario de cotización con lista de ocupantes (solo su fecha de nacimiento)
export default function Cotizacion() {
  const [resultado, setResultado] = useState<CotizacionResponse | null>(null);
  const [apartamentos, setApartamentos] = useState<ApartamentoResumen[]>([]);
  const [ultimaSolicitud, setUltimaSolicitud] = useState<FormularioCotizacion | null>(null);

  useEffect(() => {
    const cargar = () => { void listarApartamentos().then(setApartamentos); };
    cargar();
    return escucharCambiosApartamentos(cargar);
  }, []);

  useEffect(() => escucharCambiosTarifas(() => {
    if (ultimaSolicitud) {
      void cotizar(ultimaSolicitud).then(setResultado).catch(() => undefined);
    }
  }), [ultimaSolicitud]);
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioCotizacion>({
    resolver: zodResolver(esquemaCotizacion),
    mode: 'onTouched',
    defaultValues: {
      identificacionApartamento: '',
      fechaEntrada: '',
      fechaSalida: '',
      ocupantes: [{ fechaNacimiento: '' }],
    },
  });

  // Administra agregar y quitar ocupantes
  const { fields, append, remove } = useFieldArray({ control, name: 'ocupantes' });

  // El formulario ya tiene la forma de CotizacionRequest: se envía tal cual
  async function alEnviar(datos: FormularioCotizacion) {
    setResultado(null);
    setUltimaSolicitud(datos);
    try {
      setResultado(await cotizar(datos));
    } catch (error) {
      for (const [campo, mensaje] of Object.entries(erroresDeCampo(error))) {
        setError(campo as Path<FormularioCotizacion>, { message: mensaje });
      }
      setError('root.servidor', { message: mensajeDeError(error) });
    }
  }

  return (
    <section>
      <h1 className="h3 mb-3"><i className="bi bi-calculator me-2" aria-hidden="true"></i>Cotizar una estancia</h1>

      <div className="row g-4">
        <div className="col-12 col-lg-5">
          <form onSubmit={handleSubmit(alEnviar)} noValidate>
            {/* Select propio: Campo solo envuelve <input> */}
            <div className="mb-3">
              <label htmlFor="identificacionApartamento" className="form-label">Apartamento</label>
              <select
                id="identificacionApartamento"
                className={`form-select ${errors.identificacionApartamento ? 'is-invalid' : ''}`}
                {...register('identificacionApartamento')}
              >
                <option value="">Seleccione…</option>
                {apartamentos.filter((apartamento) => apartamento.activo).map((a) => (
                  <option key={a.identificacion} value={a.identificacion}>{a.identificacion} — {a.nombre}</option>
                ))}
              </select>
              <div className="invalid-feedback">{errors.identificacionApartamento?.message}</div>
            </div>

            <div className="row">
              <div className="col-6">
                <Campo id="fechaEntrada" etiqueta="Entrada" type="date"
                  error={errors.fechaEntrada?.message} {...register('fechaEntrada')} />
              </div>
              <div className="col-6">
                <Campo id="fechaSalida" etiqueta="Salida" type="date"
                  error={errors.fechaSalida?.message} {...register('fechaSalida')} />
              </div>
            </div>

            <fieldset>
              <legend className="h6">Fechas de nacimiento de los ocupantes</legend>
              {fields.map((campo, indice) => (
                <div key={campo.id} className="d-flex gap-2 align-items-start">
                  <div className="flex-grow-1">
                    <Campo id={`nacimiento-${indice}`} etiqueta={`Ocupante ${indice + 1}`} type="date"
                      error={errors.ocupantes?.[indice]?.fechaNacimiento?.message}
                      {...register(`ocupantes.${indice}.fechaNacimiento`)} />
                  </div>
                  {indice > 0 && (
                    <button type="button" className="btn btn-outline-danger mt-4" aria-label="Quitar ocupante"
                      onClick={() => remove(indice)}>
                      <i className="bi bi-x-lg" aria-hidden="true"></i>
                    </button>
                  )}
                </div>
              ))}
              {errors.ocupantes?.root && <div className="text-danger small mb-2">{errors.ocupantes.root.message}</div>}
              <button type="button" className="btn btn-outline-secondary btn-sm mb-3"
                onClick={() => append({ fechaNacimiento: '' })}>
                <i className="bi bi-person-plus me-1" aria-hidden="true"></i>Agregar ocupante
              </button>
            </fieldset>

            <div className="d-grid">
              <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                {isSubmitting && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>}
                {isSubmitting ? 'Cotizando…' : 'Cotizar'}
              </button>
            </div>

            {errors.root?.servidor && <div className="alert alert-danger mt-3">{errors.root.servidor.message}</div>}
          </form>
        </div>

        <div className="col-12 col-lg-7">
          {resultado ? (
            <article className="card shadow-sm">
              <div className="card-header d-flex justify-content-between align-items-center">
                <span>{resultado.cantidadNoches} noche(s) en {resultado.identificacionApartamento}</span>
                <strong className="fs-5">{formatearPesos(resultado.valorTotal)}</strong>
              </div>
              <div className="table-responsive">
                <table className="table table-sm mb-0">
                  <thead>
                    <tr><th>Noche</th><th>Temporada</th><th className="text-end">Tarifa</th><th className="text-center">Facturables</th><th className="text-end">Subtotal</th></tr>
                  </thead>
                  <tbody>
                    {resultado.desgloseNoches.map((noche) => (
                      <tr key={noche.fecha}>
                        <td>{formatearFecha(noche.fecha)}</td>
                        <td>{noche.nombreTemporada}</td>
                        <td className="text-end">{formatearPesos(noche.tarifaAplicada)}</td>
                        <td className="text-center">{noche.ocupantesFacturables}</td>
                        <td className="text-end">{formatearPesos(noche.subtotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </article>
          ) : (
            <div className="text-center text-body-secondary py-5">
              <i className="bi bi-receipt fs-1 d-block mb-2" aria-hidden="true"></i>
              Complete el formulario para ver el valor noche a noche.
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
