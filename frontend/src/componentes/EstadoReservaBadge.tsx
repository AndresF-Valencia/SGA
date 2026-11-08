// Indicador de estado de una reserva (componente de presentación). Los estados son los de
// EstadoReserva.java; lo usan TarjetaReserva y, más adelante, el detalle y el folio.
import { ETIQUETA_ESTADO, type EstadoReserva } from '../modelos/reserva';

// Record obliga a cubrir los seis estados: si aparece uno nuevo, TypeScript lo señala
const COLOR_ESTADO: Record<EstadoReserva, string> = {
  PENDIENTE: 'text-bg-warning',
  CONFIRMADA: 'text-bg-primary',
  EN_CURSO: 'text-bg-success',
  FINALIZADA: 'text-bg-secondary',
  CANCELADA: 'text-bg-danger',
  NO_SHOW: 'text-bg-dark',
};

interface EstadoReservaBadgeProps {
  estado: EstadoReserva;
}

// Pinta el estado con su color; no consulta nada ni guarda estado
export function EstadoReservaBadge({ estado }: EstadoReservaBadgeProps) {
  return <span className={`badge ${COLOR_ESTADO[estado]}`}>{ETIQUETA_ESTADO[estado]}</span>;
}
