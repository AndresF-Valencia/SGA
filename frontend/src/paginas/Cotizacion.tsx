// Página "/cotizacion" (pública, 7.4): valida con esquemaCotizacion y llama a
// cotizacionServicio (POST /api/cotizaciones). Muestra el total y el desglose por noche.
// Llega con ?apartamento=&fechaEntrada=&fechaSalida=&personas= desde la búsqueda o el detalle,
// y desde aquí se pasa a la reserva con los mismos datos.
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useFieldArray, useForm, type Path } from 'react-hook-form';
import { Link, useSearchParams } from 'react-router';
import { Campo } from '../componentes/Campo';
import { Puerta } from '../componentes/Puerta';
import { esquemaCotizacion, type FormularioCotizacion } from '../esquemas/cotizacion';
import { useAuth } from '../hooks/useAuth';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { escucharCambiosApartamentos, listarApartamentos } from '../servicios/apartamentoServicio';
import { escucharCambiosTarifas } from '../servicios/administracionServicio';
import type { CotizacionResponse } from '../modelos/cotizacion';
import { cotizar } from '../servicios/cotizacionServicio';
import { enlaceReserva, leerNacimientos, leerPersonas } from '../utilidades/enlaces';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';
import { formatearFecha, formatearPesos } from '../utilidades/formato';

// Formulario de cotización con lista de ocupantes (solo su fecha de nacimiento)
export default function Cotizacion() {
  const { autenticado, tieneRol } = useAuth();
  const [parametros] = useSearchParams();
  const [resultado, setResultado] = useState<CotizacionResponse | null>(null);
  const [apartamentos, setApartamentos] = useState<ApartamentoResumen[]>([]);
  const [ultimaSolicitud, setUltimaSolicitud] = useState<FormularioCotizacion | null>(null);

  // Datos que traía la página anterior
  const apartamentoInicial = parametros.get('apartamento') ?? '';
  const nacimientosIniciales = leerNacimientos(parametros.get('nacimientos'));
  const personas = leerPersonas(parametros.get('personas')) ?? 1;
  const ocupantesIniciales = nacimientosIniciales.length > 0
    ? nacimientosIniciales.map((fechaNacimiento) => ({ fechaNacimiento }))
    : Array.from({ length: personas }, () => ({ fechaNacimiento: '' }));

  const {
    register,
    control,
    handleSubmit,
    setError,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<FormularioCotizacion>({
    resolver: zodResolver(esquemaCotizacion),
    mode: 'onTouched',
    defaultValues: {
      identificacionApartamento: apartamentoInicial,
      fechaEntrada: parametros.get('fechaEntrada') ?? '',
      fechaSalida: parametros.get('fechaSalida') ?? '',
      ocupantes: ocupantesIniciales,
    },
  });

  useEffect(() => {
    const cargar = () => { void listarApartamentos().then(setApartamentos); };
    cargar();
    return escucharCambiosApartamentos(cargar);
  }, []);

  // Las opciones del select llegan después de pintar el formulario: al llegar se vuelve a
  // marcar el apartamento elegido, si no el navegador lo deja en "Elige…"
  useEffect(() => {
    if (apartamentoInicial && apartamentos.length > 0) setValue('identificacionApartamento', apartamentoInicial);
  }, [apartamentos, apartamentoInicial, setValue]);

  useEffect(() => escucharCambiosTarifas(() => {
    if (ultimaSolicitud) {
      void cotizar(ultimaSolicitud).then(setResultado).catch(() => undefined);
    }
  }), [ultimaSolicitud]);

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

  const apartamentoCotizado = apartamentos.find((a) => a.identificacion === resultado?.identificacionApartamento);
  // El personal de servicio no crea reservas; el visitante sin sesión pasa por el login y vuelve
  const puedeReservar = !autenticado || tieneRol('HUESPED', 'RECEPCIONISTA', 'ADMINISTRADOR');
  const enlaceReservar = ultimaSolicitud
    ? enlaceReserva({
      apartamento: ultimaSolicitud.identificacionApartamento,
      fechaEntrada: ultimaSolicitud.fechaEntrada,
      fechaSalida: ultimaSolicitud.fechaSalida,
      nacimientos: ultimaSolicitud.ocupantes.map((o) => o.fechaNacimiento),
    })
    : '/reservas/nueva';

  return (
    <section>
      <div className="titulo-pagina">
        <div>
          <h1 className="h2">Cotizar una estancia</h1>
          <p className="text-body-secondary mb-0">Mira el valor exacto, noche por noche, y reserva con los mismos datos.</p>
        </div>
      </div>

      <div className="row g-4">
        <div className="col-12 col-lg-5">
          <form className="card card-body shadow-sm" onSubmit={handleSubmit(alEnviar)} noValidate>
            {/* Select propio: Campo solo envuelve <input> */}
            <div className="mb-3">
              <label htmlFor="identificacionApartamento" className="form-label">Apartamento</label>
              <select
                id="identificacionApartamento"
                className={`form-select ${errors.identificacionApartamento ? 'is-invalid' : ''}`}
                {...register('identificacionApartamento')}
              >
                <option value="">Elige un apartamento</option>
                {apartamentos.filter((apartamento) => apartamento.activo).map((a) => (
                  <option key={a.identificacion} value={a.identificacion}>
                    {a.nombre} ({a.identificacion}), hasta {a.capacidadMaxima} personas
                  </option>
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
              <legend className="h6 mb-1">¿Quiénes se hospedan?</legend>
              <p className="small text-body-secondary">La fecha de nacimiento define quién paga tarifa de niño o adulto.</p>
              {fields.map((campo, indice) => (
                <div key={campo.id} className="d-flex gap-2 align-items-start">
                  <div className="flex-grow-1">
                    <Campo id={`nacimiento-${indice}`} etiqueta={`Persona ${indice + 1}`} type="date"
                      error={errors.ocupantes?.[indice]?.fechaNacimiento?.message}
                      {...register(`ocupantes.${indice}.fechaNacimiento`)} />
                  </div>
                  {indice > 0 && (
                    <button type="button" className="btn btn-outline-danger mt-4" aria-label={`Quitar persona ${indice + 1}`}
                      onClick={() => remove(indice)}>
                      <i className="bi bi-x-lg" aria-hidden="true"></i>
                    </button>
                  )}
                </div>
              ))}
              {errors.ocupantes?.root && <div className="text-danger small mb-2">{errors.ocupantes.root.message}</div>}
              <button type="button" className="btn btn-outline-secondary btn-sm mb-3"
                onClick={() => append({ fechaNacimiento: '' })}>
                <i className="bi bi-person-plus me-1" aria-hidden="true"></i>Agregar persona
              </button>
            </fieldset>

            <div className="d-grid">
              <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                {isSubmitting && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>}
                {isSubmitting ? 'Calculando…' : resultado ? 'Volver a cotizar' : 'Cotizar'}
              </button>
            </div>

            {errors.root?.servidor && <div className="alert alert-danger mt-3 mb-0">{errors.root.servidor.message}</div>}
          </form>
        </div>

        <div className="col-12 col-lg-7">
          {resultado ? (
            <article className="card shadow-sm">
              <div className="card-body d-flex flex-wrap gap-3 align-items-center justify-content-between">
                <div className="d-flex gap-3 align-items-center">
                  <Puerta identificacion={resultado.identificacionApartamento} tamano="md" />
                  <div>
                    <h2 className="h4 mb-0">{apartamentoCotizado?.nombre ?? resultado.identificacionApartamento}</h2>
                    <p className="text-body-secondary mb-0">
                      {resultado.cantidadNoches === 1 ? '1 noche' : `${resultado.cantidadNoches} noches`},
                      del {formatearFecha(resultado.desgloseNoches[0]?.fecha ?? '')}
                    </p>
                  </div>
                </div>
                <div className="text-end">
                  <div className="small text-body-secondary">Total de la estancia</div>
                  <strong className="fs-3 cifra">{formatearPesos(resultado.valorTotal)}</strong>
                </div>
              </div>

              <div className="table-responsive">
                <table className="table table-sm mb-0">
                  <thead>
                    <tr><th className="ps-3">Noche</th><th>Temporada</th><th className="text-end">Tarifa</th><th className="text-center">Personas que pagan</th><th className="text-end pe-3">Subtotal</th></tr>
                  </thead>
                  <tbody>
                    {resultado.desgloseNoches.map((noche) => (
                      <tr key={noche.fecha}>
                        <td className="ps-3">{formatearFecha(noche.fecha)}</td>
                        <td>{noche.nombreTemporada}</td>
                        <td className="text-end">{formatearPesos(noche.tarifaAplicada)}</td>
                        <td className="text-center">{noche.ocupantesFacturables}</td>
                        <td className="text-end pe-3">{formatearPesos(noche.subtotal)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="card-footer bg-transparent">
                {puedeReservar ? (
                  <div className="d-flex flex-wrap gap-3 align-items-center justify-content-between py-1">
                    <p className="mb-0 text-body-secondary small" style={{ maxWidth: '38ch' }}>
                      {autenticado
                        ? 'Pasa a la reserva con estos datos ya llenos. Solo faltan los nombres y documentos.'
                        : 'Para reservar necesitas una cuenta. Al ingresar vuelves aquí con estos datos.'}
                    </p>
                    <Link to={enlaceReservar} className="btn btn-primary">
                      Reservar con estos datos<i className="bi bi-arrow-right ms-2" aria-hidden="true"></i>
                    </Link>
                  </div>
                ) : (
                  <p className="mb-0 small text-body-secondary py-1">Tu rol no crea reservas; la recepción puede hacerlo.</p>
                )}
              </div>
            </article>
          ) : (
            <div className="text-center text-body-secondary py-5">
              <i className="bi bi-receipt fs-1 d-block mb-2" aria-hidden="true"></i>
              Llena los datos y presiona <strong>Cotizar</strong> para ver el valor noche a noche.
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
