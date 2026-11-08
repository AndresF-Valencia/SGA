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