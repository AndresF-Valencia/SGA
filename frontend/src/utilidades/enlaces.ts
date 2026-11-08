// Enlaces que llevan los datos de una página a la siguiente por la URL
// (búsqueda → detalle → cotización → reserva), para no volver a escribirlos.

interface DatosEstancia {
  apartamento?: string;
  fechaEntrada?: string | null;
  fechaSalida?: string | null;
  personas?: number | string | null;
  nacimientos?: string[];
}

// Arma "?clave=valor&..." solo con los datos que sí vienen
function consulta(datos: DatosEstancia): string {
  const parametros = new URLSearchParams();
  if (datos.apartamento) parametros.set('apartamento', datos.apartamento);
  if (datos.fechaEntrada) parametros.set('fechaEntrada', datos.fechaEntrada);
  if (datos.fechaSalida) parametros.set('fechaSalida', datos.fechaSalida);
  if (datos.personas) parametros.set('personas', String(datos.personas));
  const nacimientos = (datos.nacimientos ?? []).filter(Boolean);
  if (nacimientos.length > 0) parametros.set('nacimientos', nacimientos.join(','));
  const texto = parametros.toString();
  return texto ? `?${texto}` : '';
}

export function enlaceDetalle(identificacion: string, datos: DatosEstancia = {}): string {
  return `/apartamentos/${identificacion}${consulta(datos)}`;
}

export function enlaceCotizacion(datos: DatosEstancia): string {
  return `/cotizacion${consulta(datos)}`;
}

export function enlaceReserva(datos: DatosEstancia): string {
  return `/reservas/nueva${consulta(datos)}`;
}

// Lee la lista de fechas de nacimiento que viaja como "2000-01-01,2010-05-03"
export function leerNacimientos(texto: string | null): string[] {
  return (texto ?? '').split(',').map((v) => v.trim()).filter((v) => /^\d{4}-\d{2}-\d{2}$/.test(v));
}

// Lee la cantidad de personas (1 a 20); si no viene o no es válida, devuelve null
export function leerPersonas(texto: string | null): number | null {
  const n = Number(texto);
  return Number.isInteger(n) && n >= 1 && n <= 20 ? n : null;
}
