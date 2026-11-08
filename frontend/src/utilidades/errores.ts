// Traduce los errores de Axios al formato ErrorResponse del backend (modelos/error.ts).
// Todas las páginas con formulario lo usan en su catch: mensaje general y errores por campo.
import axios from 'axios';
import type { ErrorResponse } from '../modelos/error';

// Mensaje para el usuario: el de negocio si el backend lo envió, o uno genérico
export function mensajeDeError(error: unknown): string {
  if (!axios.isAxiosError<ErrorResponse>(error)) {
    return 'Algo salió mal. Intenta de nuevo en un momento.';
  }
  if (!error.response) {
    // Backend apagado, URL mal escrita o CORS bloqueado
    return 'No pudimos conectar con el servidor. Revisa tu conexión e intenta de nuevo en un momento.';
  }
  const { status, data } = error.response;
  if (data?.message) {
    return data.message;
  }
  if (status === 403) {
    return 'Tu cuenta no tiene permiso para hacer esto.';
  }
  return `El servidor respondió con un error (${status}). Intenta de nuevo en un momento.`;
}

// Código HTTP de la respuesta, o undefined si no hubo respuesta
export function estadoHttp(error: unknown): number | undefined {
  return axios.isAxiosError(error) ? error.response?.status : undefined;
}

// Errores de campo de un 400. El backend envía ["ocupantes[1].documento: mensaje", ...];
// se devuelven con la notación de React Hook Form: { "ocupantes.1.documento": "mensaje" }
export function erroresDeCampo(error: unknown): Record<string, string> {
  if (!axios.isAxiosError<ErrorResponse>(error)) {
    return {};
  }
  const detalles = error.response?.data?.detallesValidacion ?? [];
  const resultado: Record<string, string> = {};
  for (const detalle of detalles) {
    const separador = detalle.indexOf(': ');
    if (separador < 0) continue;
    const campo = detalle.slice(0, separador).replace(/\[(\d+)\]/g, '.$1');
    resultado[campo] = detalle.slice(separador + 2);
  }
  return resultado;
}
