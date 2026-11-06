// Página "/reservas/nueva" (recepción, canal DIRECTO): valida con esquemaNuevaReserva,
// convierte el formulario a CrearReservaRequest y lo envía con reservaServicio (POST /api/reservas, JWT).
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useFieldArray, useForm, type Path } from 'react-hook-form';
import { esquemaNuevaReserva, type FormularioNuevaReserva } from '../esquemas/reserva';
import type { CrearReservaRequest } from '../modelos/reserva';
import { crearReserva } from '../servicios/reservaServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';

const OCUPANTE_VACIO = { documento: '', nombre: '', fechaNacimiento: '' };

// Del formulario al DTO: el ocupante 0 es el titular y va también dentro de ocupantes
function aSolicitud(datos: FormularioNuevaReserva): CrearReservaRequest {
  return {
    apartamentoId: datos.apartamentoId,
    fechaEntrada: datos.fechaEntrada,
    fechaSalida: datos.fechaSalida,
    horaEstimadaLlegada: datos.horaEstimadaLlegada,
    canalOrigen: 'DIRECTO',
    titular: { ...datos.ocupantes[0], email: datos.emailTitular, telefono: datos.telefonoTitular },
    ocupantes: datos.ocupantes,
    cantidadMascotas: datos.cantidadMascotas,
  };
}

// Campo del backend → campo del formulario (titular.email → emailTitular, titular.x → ocupantes.0.x)
function aCampoFormulario(campo: string): string {
  if (campo === 'titular.email') return 'emailTitular';
  if (campo === 'titular.telefono') return 'telefonoTitular';
  return campo.replace(/^titular\./, 'ocupantes.0.');
}

// Formulario con lista dinámica de ocupantes y errores del servidor por campo y generales
export default function NuevaReserva() {
  const [codigoCreado, setCodigoCreado] = useState<string | null>(null);
  const {
    register,
    control,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioNuevaReserva>({
    resolver: zodResolver(esquemaNuevaReserva),
    mode: 'onTouched',
    defaultValues: {
      apartamentoId: '',
      fechaEntrada: '',
      fechaSalida: '',
      horaEstimadaLlegada: '',
      emailTitular: '',
      telefonoTitular: '',
      cantidadMascotas: 0,
      ocupantes: [OCUPANTE_VACIO],
    },
  });

  // Administra agregar y quitar ocupantes
  const { fields, append, remove } = useFieldArray({ control, name: 'ocupantes' });

  // Envía la reserva; 400 se pinta junto al campo, 401/404/409 como mensaje general
  async function alEnviar(datos: FormularioNuevaReserva) {
    setCodigoCreado(null);
    try {
      const reserva = await crearReserva(aSolicitud(datos));
      setCodigoCreado(reserva.codigoReserva);
      reset();
    } catch (error) {
      for (const [campo, mensaje] of Object.entries(erroresDeCampo(error))) {
        setError(aCampoFormulario(campo) as Path<FormularioNuevaReserva>, { message: mensaje });
      }
      setError('root.servidor', { message: mensajeDeError(error) });
    }
  }

  return (
    <section>
      <h1>Nueva reserva (recepción)</h1>

      <form onSubmit={handleSubmit(alEnviar)} noValidate>
        <div>
          <label htmlFor="apartamentoId">Apartamento</label>
          <input id="apartamentoId" placeholder="AP001" {...register('apartamentoId')} />
          {errors.apartamentoId && <p className="error">{errors.apartamentoId.message}</p>}
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

        <div>
          <label htmlFor="horaEstimadaLlegada">Hora estimada de llegada</label>
          <input id="horaEstimadaLlegada" type="time" {...register('horaEstimadaLlegada')} />
          {errors.horaEstimadaLlegada && <p className="error">{errors.horaEstimadaLlegada.message}</p>}
        </div>

        <div>
          <label htmlFor="cantidadMascotas">Mascotas</label>
          <input id="cantidadMascotas" type="number" min={0} {...register('cantidadMascotas', { valueAsNumber: true })} />
          {errors.cantidadMascotas && <p className="error">{errors.cantidadMascotas.message}</p>}
        </div>

        <fieldset>
          <legend>Ocupantes (el primero es el titular)</legend>

          {fields.map((campo, indice) => (
            <div key={campo.id} className="tarjeta">
              <strong>{indice === 0 ? 'Titular' : `Ocupante ${indice + 1}`}</strong>

              <label htmlFor={`documento-${indice}`}>Documento</label>
              <input id={`documento-${indice}`} {...register(`ocupantes.${indice}.documento`)} />
              {errors.ocupantes?.[indice]?.documento && (
                <p className="error">{errors.ocupantes[indice].documento.message}</p>
              )}

              <label htmlFor={`nombre-${indice}`}>Nombre</label>
              <input id={`nombre-${indice}`} {...register(`ocupantes.${indice}.nombre`)} />
              {errors.ocupantes?.[indice]?.nombre && (
                <p className="error">{errors.ocupantes[indice].nombre.message}</p>
              )}

              <label htmlFor={`fechaNacimiento-${indice}`}>Fecha de nacimiento</label>
              <input id={`fechaNacimiento-${indice}`} type="date" {...register(`ocupantes.${indice}.fechaNacimiento`)} />
              {errors.ocupantes?.[indice]?.fechaNacimiento && (
                <p className="error">{errors.ocupantes[indice].fechaNacimiento.message}</p>
              )}

              {/* Solo el titular lleva correo y teléfono (CrearReservaRequest.TitularDto) */}
              {indice === 0 && (
                <>
                  <label htmlFor="emailTitular">Correo del titular</label>
                  <input id="emailTitular" type="email" {...register('emailTitular')} />
                  {errors.emailTitular && <p className="error">{errors.emailTitular.message}</p>}

                  <label htmlFor="telefonoTitular">Teléfono del titular</label>
                  <input id="telefonoTitular" type="tel" {...register('telefonoTitular')} />
                  {errors.telefonoTitular && <p className="error">{errors.telefonoTitular.message}</p>}
                </>
              )}

              {indice > 0 && (
                <button type="button" onClick={() => remove(indice)}>Quitar</button>
              )}
            </div>
          ))}

          {errors.ocupantes?.root && <p className="error">{errors.ocupantes.root.message}</p>}
          <button type="button" onClick={() => append(OCUPANTE_VACIO)}>Agregar ocupante</button>
        </fieldset>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creando…' : 'Crear reserva'}
        </button>

        {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
        {codigoCreado && <p className="exito">Reserva {codigoCreado} creada en estado PENDIENTE.</p>}
      </form>
    </section>
  );
}
