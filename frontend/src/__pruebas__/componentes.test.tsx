// Componentes que se pintan sin backend: la tarjeta de reserva y la página 404.
import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, describe, expect, it } from 'vitest';
import { TarjetaReserva } from '../componentes/TarjetaReserva';
import type { ReservaResumen } from '../modelos/reserva';
import NoEncontrada from '../paginas/NoEncontrada';

afterEach(cleanup);

const reserva = {
  codigoReserva: 'RES-2026-00007',
  estadoReserva: 'PENDIENTE',
  nombreTitular: 'Laura Gómez',
  apartamentoId: 'AP004',
  fechaEntrada: '2026-11-28',
  fechaSalida: '2026-12-01',
  canalOrigen: 'PORTAL',
  valorTotal: 975000,
} as ReservaResumen;

describe('TarjetaReserva', () => {
  it('muestra código con enlace, titular, apartamento y valor', () => {
    render(<MemoryRouter><TarjetaReserva reserva={reserva} /></MemoryRouter>);
    expect(screen.getByRole('link', { name: 'RES-2026-00007' }).getAttribute('href')).toBe('/reservas/RES-2026-00007');
    expect(screen.getByText('Laura Gómez')).toBeTruthy();
    expect(screen.getByText(/Apartamento AP004/)).toBeTruthy();
    expect(screen.getByText(/975\.000/)).toBeTruthy();
  });

  it('solo pinta la fila de acciones cuando hay acciones', () => {
    const { container } = render(<MemoryRouter><TarjetaReserva reserva={reserva} /></MemoryRouter>);
    expect(container.querySelectorAll('.card-body').length).toBe(1);
  });
});

describe('NoEncontrada', () => {
  it('ofrece volver al inicio o buscar fechas', () => {
    render(<MemoryRouter><NoEncontrada /></MemoryRouter>);
    expect(screen.getByRole('heading', { name: 'Esta puerta no lleva a ningún lado' })).toBeTruthy();
    expect(screen.getByRole('link', { name: 'Ir al inicio' }).getAttribute('href')).toBe('/');
    expect(screen.getByRole('link', { name: 'Buscar fechas' }).getAttribute('href')).toBe('/disponibilidad');
  });
});
