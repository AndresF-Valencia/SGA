import { useMemo, useReducer, type ReactNode } from 'react';
import { ContextoFiltros, FILTROS_INICIALES, reductorFiltros } from './contextoFiltros';

export function FiltrosProvider({ children }: { children: ReactNode }) {
  const [filtros, despachar] = useReducer(reductorFiltros, FILTROS_INICIALES);
  const valor = useMemo(() => ({ filtros, despachar }), [filtros]);
  return <ContextoFiltros value={valor}>{children}</ContextoFiltros>;
}