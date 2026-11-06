import { createContext, type Dispatch } from 'react';
import type { EstadoReserva } from '../modelos/reserva';

export interface FiltrosReservas {
  estado: EstadoReserva | '';
  documentoTitular: string;
  pagina: number;
}

export const FILTROS_INICIALES: FiltrosReservas = {
  estado: '',
  documentoTitular: '',
  pagina: 0,
};

export type AccionFiltros =
  | { tipo: 'cambiarEstado'; estado: EstadoReserva | '' }
  | { tipo: 'cambiarDocumentoTitular'; documentoTitular: string }
  | { tipo: 'cambiarPagina'; pagina: number }
  | { tipo: 'limpiar' };

export function reductorFiltros(filtros: FiltrosReservas, accion: AccionFiltros): FiltrosReservas {
  switch (accion.tipo) {
    case 'cambiarEstado':
      return { ...filtros, estado: accion.estado, pagina: 0 };
    case 'cambiarDocumentoTitular':
      return { ...filtros, documentoTitular: accion.documentoTitular, pagina: 0 };
    case 'cambiarPagina':
      return { ...filtros, pagina: accion.pagina };
    case 'limpiar':
      return FILTROS_INICIALES;
  }
}

export interface ValorFiltros {
  filtros: FiltrosReservas;
  despachar: Dispatch<AccionFiltros>;
}

export const ContextoFiltros = createContext<ValorFiltros | null>(null);