// Página "/cotizacion" (pública, 7.4): valida con esquemaCotizacion y llama a
// cotizacionServicio (POST /api/cotizaciones). Muestra el total y el desglose por noche.
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useFieldArray, useForm, type Path } from 'react-hook-form';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';
import { esquemaCotizacion, type FormularioCotizacion } from '../esquemas/cotizacion';
import type { CotizacionResponse } from '../modelos/cotizacion';
import { cotizar } from '../servicios/cotizacionServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';

// Formato de pesos colombianos sin decimales
const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

// Formulario de cotización con lista de ocupantes (solo su fecha de nacimiento)
export default function Cotizacion() {
  const [resultado, setResultado] = useState<CotizacionResponse | null>(null);
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
      <h1>Cotizar una estancia</h1>

      <form onSubmit={handleSubmit(alEnviar)} noValidate>
        <div>
          <label htmlFor="identificacionApartamento">Apartamento</label>
          <select id="identificacionApartamento" {...register('identificacionApartamento')}>
            <option value="">Seleccione…</option>
            {apartamentosEjemplo.map((a) => (
              <option key={a.identificacion} value={a.identificacion}>
                {a.identificacion} — {a.nombre}
              </option>
            ))}
          </select>
          {errors.identificacionApartamento && <p className="error">{errors.identificacionApartamento.message}</p>}
        </div>

        <div>
          <label htmlFor="fechaEntrada">Fecha de entrada</label>
          <input id="fechaEntrada" type="date" {...register('fechaEntrada')} />
          {errors.fechaEntrada && <p className="error">{errors.fechaEntrada.message}</p>}
        </div>

        <div>
          <label htmlFor="fechaSalida">Fecha de salida</label>
          <input id="fechaSalida" type="date" {...register('fechaSalida')} />
          {errors.fechaSalida && <p className="error">{errors.fechaSalida.message}</p>}
        </div>

        <fieldset>
          <legend>Fechas de nacimiento de los ocupantes</legend>
          {fields.map((campo, indice) => (
            <div key={campo.id}>
              <label htmlFor={`nacimiento-${indice}`}>Ocupante {indice + 1}</label>
              <input id={`nacimiento-${indice}`} type="date" {...register(`ocupantes.${indice}.fechaNacimiento`)} />
              {indice > 0 && (
                <button type="button" onClick={() => remove(indice)}>Quitar</button>
              )}
              {errors.ocupantes?.[indice]?.fechaNacimiento && (
                <p className="error">{errors.ocupantes[indice].fechaNacimiento.message}</p>
              )}
            </div>
          ))}
          {errors.ocupantes?.root && <p className="error">{errors.ocupantes.root.message}</p>}
          <button type="button" onClick={() => append({ fechaNacimiento: '' })}>Agregar ocupante</button>
        </fieldset>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Cotizando…' : 'Cotizar'}
        </button>

        {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
      </form>

      {resultado && (
        <article className="tarjeta">
          <h2>Total: {pesos.format(resultado.valorTotal)}</h2>
          <p>{resultado.cantidadNoches} noche(s) en {resultado.identificacionApartamento}</p>
          <table>
            <thead>
              <tr><th>Noche</th><th>Temporada</th><th>Tarifa</th><th>Facturables</th><th>Subtotal</th></tr>
            </thead>
            <tbody>
              {resultado.desgloseNoches.map((noche) => (
                <tr key={noche.fecha}>
                  <td>{noche.fecha}</td>
                  <td>{noche.nombreTemporada}</td>
                  <td>{pesos.format(noche.tarifaAplicada)}</td>
                  <td>{noche.ocupantesFacturables}</td>
                  <td>{pesos.format(noche.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </article>
      )}
    </section>
  );
}
