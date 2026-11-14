import type { ApartamentoResumen } from './apartamento';
import type { Pagina } from './pagina';
import type { Rol, UsuarioResponse } from './usuario';

export type EstadoActivo = 'TODOS' | 'ACTIVOS' | 'INACTIVOS';
export type TipoTemporada = 'BASE' | 'MEDIA' | 'ALTA';
export type MotivoBloqueo = 'MANTENIMIENTO' | 'REMODELACION' | 'FUERA_DE_SERVICIO';

export interface UsuarioAdmin extends UsuarioResponse {}
export type PaginaUsuarios = Pagina<UsuarioAdmin>;

export interface Temporada {
  id: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  estanciaMinima: number;
  tipoTemporada: TipoTemporada;
}

export interface Bloqueo {
  id: string;
  identificacionApartamento: string;
  fechaInicio: string;
  fechaFin: string;
  motivo: string;
  estado: 'VIGENTE' | 'LEVANTADO';
}

export interface ConflictoCanal {
  id: string;
  canal: string;
  codigoExterno: string;
  apartamento: string;
  fechaEntrada: string;
  fechaSalida: string;
  titularNombre: string;
  titularDocumento: string;
  motivo: string;
  estado: string;
  resolucion: string | null;
}

export type ApartamentoAdmin = ApartamentoResumen;
export type RolAdmin = Rol;

// Espejo de NovedadResponse.java: daño o incidencia reportada en un apartamento
export interface Novedad {
  identificacionApartamento: string;
  gravedad: string;
  descripcion: string;
  fechaRegistro: string;
  reportadoPor: string;
}

// Espejo de ReporteResponse.java (GET /api/reportes)
export interface ConteoReporte { nombre: string; cantidad: number }
export interface OcupacionApartamento { identificacion: string; nombre: string; nochesOcupadas: number; porcentajeOcupacion: number }
export interface Reporte {
  desde: string;
  hasta: string;
  nochesDisponibles: number;
  nochesOcupadas: number;
  porcentajeOcupacion: number;
  ingresos: number;
  valorReservado: number;
  totalReservas: number;
  reservasPorEstado: ConteoReporte[];
  reservasPorCanal: ConteoReporte[];
  apartamentos: OcupacionApartamento[];
}
