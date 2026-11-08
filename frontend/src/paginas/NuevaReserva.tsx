// Página "/reservas/nueva" (recepción, canal DIRECTO): valida con esquemaNuevaReserva,
// convierte el formulario a CrearReservaRequest y lo envía con reservaServicio (POST /api/reservas).
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useFieldArray, useForm, type Path } from 'react-hook-form';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { Campo } from '../componentes/Campo';
import { esquemaNuevaReserva, type FormularioNuevaReserva } from '../esquemas/reserva';
import type { ApartamentoResumen } from '../modelos/apartamento';
import type { CrearReservaRequest } from '../modelos/reserva';
import { listarApartamentos } from '../servicios/apartamentoServicio';
import { crearReserva } from '../servicios/reservaServicio';
import { obtenerMiPerfil } from '../servicios/usuarioServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';
import { useAuth } from '../hooks/useAuth';
import { leerNacimientos } from '../utilidades/enlaces';
import { notificarExito } from '../utilidades/notificaciones';

const OCUPANTE_VACIO = { documento: '', nombre: '', fechaNacimiento: '' };

// Del formulario al DTO: el ocupante 0 es el titular y va también dentro de ocupantes
function aSolicitud(datos: FormularioNuevaReserva, canalOrigen: 'PORTAL' | 'DIRECTO'): CrearReservaRequest {
  return {
    apartamentoId: datos.apartamentoId,
    fechaEntrada: datos.fechaEntrada,
    fechaSalida: datos.fechaSalida,
    horaEstimadaLlegada: datos.horaEstimadaLlegada,
    canalOrigen,
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
  const { tieneRol } = useAuth();
  const esHuesped = tieneRol('HUESPED');
  const [apartamentos, setApartamentos] = useState<ApartamentoResumen[]>([]);
  const navegar = useNavigate();
  // Desde el detalle o la cotización se llega con
  // ?apartamento=AP001&fechaEntrada=...&fechaSalida=...&nacimientos=2000-01-01,2012-05-03
  const [parametros] = useSearchParams();
  const apartamentoInicial = parametros.get('apartamento') ?? '';
  const fechaEntradaInicial = parametros.get('fechaEntrada') ?? '';
  const fechaSalidaInicial = parametros.get('fechaSalida') ?? '';
  const nacimientos = leerNacimientos(parametros.get('nacimientos'));
  const ocupantesIniciales = nacimientos.length > 0
    ? nacimientos.map((fechaNacimiento) => ({ ...OCUPANTE_VACIO, fechaNacimiento }))
    : [OCUPANTE_VACIO];
  const {
    register,
    control,
    handleSubmit,
    reset,
    setError,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<FormularioNuevaReserva>({
    resolver: zodResolver(esquemaNuevaReserva),
    mode: 'onTouched',
    defaultValues: {
      apartamentoId: apartamentoInicial,
      fechaEntrada: fechaEntradaInicial,
      fechaSalida: fechaSalidaInicial,
      horaEstimadaLlegada: '',
      emailTitular: '',
      telefonoTitular: '',
      cantidadMascotas: 0,
      ocupantes: ocupantesIniciales,
    },
  });

  useEffect(() => {
    listarApartamentos().then((lista) => setApartamentos(lista.filter((a) => a.activo))).catch(() => setApartamentos([]));
  }, []);

  // Las opciones del select llegan después de pintar el formulario: al llegar se vuelve a
  // marcar el apartamento que venía en la URL, si no el navegador lo deja vacío
  useEffect(() => {
    if (apartamentoInicial && apartamentos.length > 0) setValue('apartamentoId', apartamentoInicial);
  }, [apartamentos, apartamentoInicial, setValue]);

  // Administra agregar y quitar ocupantes
  const { fields, append, remove } = useFieldArray({ control, name: 'ocupantes' });

  useEffect(() => {
    if (!esHuesped) return;
    obtenerMiPerfil().then((perfil) => {
      // El huésped es el titular. De las fechas que traía la cotización se quita la suya
      // (o la primera, si no aparece) y las demás quedan como acompañantes.
      const otras = [...nacimientos];
      const suya = otras.indexOf(perfil.fechaNacimiento);
      otras.splice(suya >= 0 ? suya : 0, 1);
      reset({
        apartamentoId: apartamentoInicial,
        fechaEntrada: fechaEntradaInicial,
        fechaSalida: fechaSalidaInicial,
        horaEstimadaLlegada: '',
        emailTitular: perfil.correo,
        telefonoTitular: perfil.telefono,
        cantidadMascotas: 0,
        ocupantes: [
          { documento: perfil.documento, nombre: perfil.nombre, fechaNacimiento: perfil.fechaNacimiento },
          ...otras.map((fechaNacimiento) => ({ ...OCUPANTE_VACIO, fechaNacimiento })),
        ],
      });
    }).catch((e: unknown) => setError('root.servidor', { message: mensajeDeError(e) }));
    // nacimientos sale de la URL; se lee una sola vez al abrir
  }, [esHuesped, reset, setError, apartamentoInicial, fechaEntradaInicial, fechaSalidaInicial]);

  // Envía la reserva; 400 se pinta junto al campo, 404/409 como mensaje general
  async function alEnviar(datos: FormularioNuevaReserva) {
    try {
      const reserva = await crearReserva(aSolicitud(datos, esHuesped ? 'PORTAL' : 'DIRECTO'));
      // Se abre la reserva creada: ahí está el folio y el pago del anticipo
      void notificarExito(`Reserva ${reserva.codigoReserva} creada. Queda pendiente hasta pagar el anticipo.`);
      navegar(`/reservas/${reserva.codigoReserva}`);
    } catch (error) {
      for (const [campo, mensaje] of Object.entries(erroresDeCampo(error))) {
        setError(aCampoFormulario(campo) as Path<FormularioNuevaReserva>, { message: mensaje });
      }
      setError('root.servidor', { message: mensajeDeError(error) });
    }
  }

  return (
    <section>
      <div className="titulo-pagina">
        <div>
          <h1 className="h2">{esHuesped ? 'Reservar un apartamento' : 'Nueva reserva en recepción'}</h1>
          <p className="text-body-secondary mb-0">
            {esHuesped
              ? 'La reserva queda pendiente hasta que se pague el anticipo.'
              : 'Registra una reserva para un huésped que llega o llama directamente.'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(alEnviar)} noValidate>
        <div className="row">
          <div className="col-12 col-md-6">
            <div className="mb-3">
              <label htmlFor="apartamentoId" className="form-label">Apartamento</label>
              <select id="apartamentoId" className={`form-select ${errors.apartamentoId ? 'is-invalid' : ''}`}
                aria-invalid={errors.apartamentoId ? true : undefined} {...register('apartamentoId')}>
                <option value="">Elige un apartamento</option>
                {apartamentos.map((a) => (
                  <option key={a.identificacion} value={a.identificacion}>
                    {a.nombre} ({a.identificacion}), hasta {a.capacidadMaxima} personas
                  </option>
                ))}
              </select>
              <div className="invalid-feedback">{errors.apartamentoId?.message}</div>
            </div>
          </div>
          <div className="col-12 col-md-6">
            <Campo id="horaEstimadaLlegada" etiqueta="Hora estimada de llegada" type="time"
              error={errors.horaEstimadaLlegada?.message} {...register('horaEstimadaLlegada')} />
          </div>
          <div className="col-12 col-md-4">
            <Campo id="fechaEntrada" etiqueta="Fecha de entrada" type="date"
              error={errors.fechaEntrada?.message} {...register('fechaEntrada')} />
          </div>
          <div className="col-12 col-md-4">
            <Campo id="fechaSalida" etiqueta="Fecha de salida" type="date"
              error={errors.fechaSalida?.message} {...register('fechaSalida')} />
          </div>
          <div className="col-12 col-md-4">
            <Campo id="cantidadMascotas" etiqueta="Mascotas" type="number" min={0}
              error={errors.cantidadMascotas?.message} {...register('cantidadMascotas', { valueAsNumber: true })} />
          </div>
        </div>

        <fieldset className="mt-2">
          <legend className="h5">Ocupantes <small className="text-body-secondary">(el primero es el titular)</small></legend>

          {fields.map((campo, indice) => (
            <div key={campo.id} className="card mb-3">
              <div className="card-header d-flex justify-content-between align-items-center">
                <strong>{indice === 0 ? 'Titular' : `Ocupante ${indice + 1}`}</strong>
                {indice > 0 && (
                  <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => remove(indice)}>
                    Quitar
                  </button>
                )}
              </div>
              <div className="card-body row">
                <div className="col-12 col-md-4">
                  <Campo id={`documento-${indice}`} etiqueta="Documento"
                    error={errors.ocupantes?.[indice]?.documento?.message}
                    readOnly={esHuesped && indice === 0}
                    {...register(`ocupantes.${indice}.documento`)} />
                </div>
                <div className="col-12 col-md-4">
                  <Campo id={`nombre-${indice}`} etiqueta="Nombre"
                    error={errors.ocupantes?.[indice]?.nombre?.message}
                    readOnly={esHuesped && indice === 0}
                    {...register(`ocupantes.${indice}.nombre`)} />
                </div>
                <div className="col-12 col-md-4">
                  <Campo id={`fechaNacimiento-${indice}`} etiqueta="Fecha de nacimiento" type="date"
                    error={errors.ocupantes?.[indice]?.fechaNacimiento?.message}
                    readOnly={esHuesped && indice === 0}
                    {...register(`ocupantes.${indice}.fechaNacimiento`)} />
                </div>

                {/* Solo el titular lleva correo y teléfono (CrearReservaRequest.TitularDto) */}
                {indice === 0 && (
                  <>
                    <div className="col-12 col-md-6">
                      <Campo id="emailTitular" etiqueta="Correo del titular" type="email"
                        error={errors.emailTitular?.message} {...register('emailTitular')} />
                    </div>
                    <div className="col-12 col-md-6">
                      <Campo id="telefonoTitular" etiqueta="Teléfono del titular" type="tel"
                        error={errors.telefonoTitular?.message} {...register('telefonoTitular')} />
                    </div>
                  </>
                )}
              </div>
            </div>
          ))}

          {errors.ocupantes?.root && <div className="text-danger small mb-2">{errors.ocupantes.root.message}</div>}
          <button type="button" className="btn btn-outline-secondary" onClick={() => append(OCUPANTE_VACIO)}>
            <i className="bi bi-person-plus me-1" aria-hidden="true"></i>Agregar ocupante
          </button>
        </fieldset>

        <div className="mt-4 d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>}
            {isSubmitting ? 'Creando…' : 'Crear reserva'}
          </button>
          <Link to={esHuesped ? '/mis-reservas' : '/reservas'} className="btn btn-outline-secondary">Volver</Link>
        </div>

        {errors.root?.servidor && <div className="alert alert-danger mt-3">{errors.root.servidor.message}</div>}
      </form>
    </section>
  );
}
