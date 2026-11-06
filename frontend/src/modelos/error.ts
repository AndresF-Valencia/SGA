// Espejo de ErrorResponse.java: la forma de TODOS los errores de la API, tanto los del
// GlobalExceptionHandler (400, 404, 409) como los de seguridad (401, 403).
// Lo interpreta utilidades/errores.ts.
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string; // mensaje con significado de negocio
  path: string;
  detallesValidacion: string[]; // solo en un 400: cada elemento es "campo: mensaje"
}
