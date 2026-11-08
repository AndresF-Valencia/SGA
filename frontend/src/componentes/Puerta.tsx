// Puerta de colores de un apartamento (ver utilidades/identidad.ts). Es decorativa:
// el nombre del apartamento siempre va escrito al lado.
import type { CSSProperties } from 'react';
import { colorPuerta } from '../utilidades/identidad';

interface PuertaProps {
  identificacion: string;
  tamano?: 'sm' | 'md' | 'lg';
}

export function Puerta({ identificacion, tamano = 'sm' }: PuertaProps) {
  return (
    <span
      className={`puerta puerta-${tamano}`}
      style={{ '--puerta': colorPuerta(identificacion) } as CSSProperties}
      aria-hidden="true"
    >
      <span className="puerta-pomo" />
    </span>
  );
}
