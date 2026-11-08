// Página "/disponibilidad" (pública, 7.5): valida con esquemaDisponibilidad y consulta
// disponibilidadServicio (GET /api/disponibilidad). Resultados en tarjetas con formatearPesos.
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState, type CSSProperties } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useSearchParams } from 'react-router';
import { Campo } from '../componentes/Campo';
import { Cargando } from '../componentes/Cargando';
import { Puerta } from '../componentes/Puerta';
import { esquemaDisponibilidad, type FormularioDisponibilidad } from '../esquemas/disponibilidad';
import type { DisponibilidadResponse } from '../modelos/disponibilidad';
import { buscarDisponibilidad } from '../servicios/disponibilidadServicio';
import { mensajeDeError } from '../utilidades/errores';
import { formatearFecha, formatearPesos } from '../utilidades/formato';
import { colorPuerta, miniatura } from '../utilidades/identidad';
import { enlaceCotizacion, enlaceDetalle } from '../utilidades/enlaces';

// Lee un número de la URL; si no viene o no es válido, usa el valor por defecto
function numeroDe(texto: string | null, porDefecto: number): number {
  const n = Number(texto);
  return texto !== null && Number.isFinite(n) ? n : porDefecto;
}

// Formulario de búsqueda y grid de apartamentos disponibles en todas las noches del rango
export default function BuscarDisponibilidad() {
  const [resultado, setResultado] = useState<DisponibilidadResponse | null>(null);
  // La búsqueda de Inicio llega con los criterios en la URL: ?fechaEntrada=...&fechaSalida=...
  const [parametros] = useSearchParams();
  const {
    register,
    handleSubmit,
    getValues,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioDisponibilidad>({
    resolver: zodResolver(esquemaDisponibilidad),
    mode: 'onTouched',
    defaultValues: {
      fechaEntrada: parametros.get('fechaEntrada') ?? '',
      fechaSalida: parametros.get('fechaSalida') ?? '',
      personas: numeroDe(parametros.get('personas'), 2),
      mascotas: numeroDe(parametros.get('mascotas'), 0),
    },
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

  // Si llegó con fechas desde Inicio, busca de una vez
  const vieneConFechas = parametros.has('fechaEntrada') && parametros.has('fechaSalida');
  useEffect(() => {
    if (vieneConFechas) void handleSubmit(alEnviar)();
    // Solo al abrir la página
  }, []);

  // Las fechas y personas buscadas viajan al detalle y a la cotización
  const estancia = resultado
    ? { fechaEntrada: resultado.fechaEntrada, fechaSalida: resultado.fechaSalida, personas: getValues('personas') }
    : {};

  return (
    <section>
      <div className="titulo-pagina">
        <div>
          <h1 className="h2">Buscar fechas</h1>
          <p className="text-body-secondary mb-0">Elige las fechas y el tamaño del grupo para ver qué apartamentos están libres.</p>
        </div>
      </div>

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
            {resultado.totalDisponibles === 1 ? '1 apartamento libre' : `${resultado.totalDisponibles} apartamentos libres`} del{' '}
            {formatearFecha(resultado.fechaEntrada)} al {formatearFecha(resultado.fechaSalida)}.
          </p>
          <div className="row g-3">
            {resultado.apartamentosDisponibles.map((apto) => (
              <div key={apto.identificacionApartamento} className="col-12 col-md-6 col-lg-4">
                <article className="card h-100 shadow-sm tarjeta-apto"
                  style={{ '--puerta': colorPuerta(apto.identificacionApartamento) } as CSSProperties}>
                  {apto.imagenes[0] && <img src={miniatura(apto.imagenes[0])} alt="" className="tarjeta-apto-foto" loading="lazy" />}
                  <div className="card-body d-flex gap-3">
                    <Puerta identificacion={apto.identificacionApartamento} tamano="md" />
                    <div>
                      <h2 className="h4 card-title mb-1">{apto.nombre}</h2>
                      <p className="text-body-secondary mb-3">
                        <i className="bi bi-people me-1" aria-hidden="true"></i>Hasta {apto.capacidadMaxima} personas
                      </p>
                      <p className="mb-0">
                        {apto.tarifaEstimadaPorNoche !== null
                          ? <><strong className="fs-5 cifra">{formatearPesos(apto.tarifaEstimadaPorNoche)}</strong> <span className="text-body-secondary">por noche, aprox.</span></>
                          : <span className="text-body-secondary">Precio por confirmar</span>}
                      </p>
                    </div>
                  </div>
                  <div className="card-footer bg-transparent d-flex gap-2">
                    <Link to={enlaceDetalle(apto.identificacionApartamento, estancia)} className="btn btn-outline-primary btn-sm">
                      Ver detalle
                    </Link>
                    <Link to={enlaceCotizacion({ ...estancia, apartamento: apto.identificacionApartamento })}
                      className="btn btn-primary btn-sm">Cotizar y reservar</Link>
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
