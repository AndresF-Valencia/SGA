// Tarjeta de una reserva (ReservaResumenResponse del backend) para ListaReservas y MisReservas.
// Usa EstadoReservaBadge, formato.ts y su CSS Module; las acciones las decide quien la usa.
import type { ReactNode } from 'react';
import type { EstadoReserva, ReservaResumen } from '../modelos/reserva';
import { formatearFecha, formatearPesos } from '../utilidades/formato';
import { EstadoReservaBadge } from './EstadoReservaBadge';
import estilos from './TarjetaReserva.module.css';

// Solo los estados activos llevan borde de color
const BORDE_ESTADO: Partial<Record<EstadoReserva, string>> = {
  PENDIENTE: estilos.pendiente,
  CONFIRMADA: estilos.confirmada,
  EN_CURSO: estilos.enCurso,
};

const TERMINALES: EstadoReserva[] = ['FINALIZADA', 'CANCELADA', 'NO_SHOW'];

interface TarjetaReservaProps {
  reserva: ReservaResumen;
  acciones?: ReactNode; // botones opcionales
}

// Código, estado, titular, apartamento, fechas, canal y valor de la reserva
export function TarjetaReserva({ reserva, acciones }: TarjetaReservaProps) {
  const clases = [
    'card h-100',
    estilos.tarjeta,
    BORDE_ESTADO[reserva.estadoReserva] ?? '',
    TERMINALES.includes(reserva.estadoReserva) ? estilos.terminada : '',
  ].join(' ');

  return (
    <article className={clases}>
      <div className="card-header d-flex justify-content-between align-items-center">
        <span className="fw-semibold">{reserva.codigoReserva}</span>
        <EstadoReservaBadge estado={reserva.estadoReserva} />
      </div>
      <div className="card-body">
        <p className="mb-1"><i className="bi bi-person me-1" aria-hidden="true"></i>{reserva.nombreTitular}</p>
        <p className="mb-1 text-body-secondary small">
          <i className="bi bi-building me-1" aria-hidden="true"></i>{reserva.apartamentoId}
        </p>
        <p className="mb-0 text-body-secondary small">
          <i className="bi bi-calendar-range me-1" aria-hidden="true"></i>
          {formatearFecha(reserva.fechaEntrada)} → {formatearFecha(reserva.fechaSalida)}
        </p>
      </div>
      <div className="card-footer d-flex justify-content-between align-items-center">
        <small className="text-body-secondary">{reserva.canalOrigen}</small>
        <strong>{formatearPesos(reserva.valorTotal)}</strong>
      </div>
      {acciones && <div className="card-body border-top d-flex gap-2">{acciones}</div>}
    </article>
  );
}
