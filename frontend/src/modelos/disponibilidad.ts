// Espejo de DisponibilidadResponse.java (GET /api/disponibilidad, pública, 7.5).
// Lo usan disponibilidadServicio y la página BuscarDisponibilidad.

// Espejo de: DisponibilidadResponse.ApartamentoDisponibleDto
export interface ApartamentoDisponible {
  identificacionApartamento: string;
  nombre: string;
  capacidadMaxima: number;
  caracteristicas: string[];
  imagenes: string[];
  tarifaEstimadaPorNoche: number | null; // null si al apartamento le falta tarifa
}

// Espejo de: record DisponibilidadResponse(...). No usa PaginaResponse: tiene su propia forma
export interface DisponibilidadResponse {
  fechaEntrada: string;
  fechaSalida: string;
  totalDisponibles: number;
  apartamentosDisponibles: ApartamentoDisponible[];
  pagina: number;
  tamano: number;
  totalPaginas: number;
}

// Parámetros de la consulta (los mismos @RequestParam de DisponibilidadController)
export interface CriteriosDisponibilidad {
  fechaEntrada: string;
  fechaSalida: string;
  personas: number;
  mascotas: number;
}
