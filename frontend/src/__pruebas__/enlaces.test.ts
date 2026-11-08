// Los datos de la estancia viajan por la URL entre búsqueda, detalle, cotización y reserva.
import { describe, expect, it } from 'vitest';
import { enlaceCotizacion, enlaceDetalle, enlaceReserva, leerNacimientos, leerPersonas } from '../utilidades/enlaces';

describe('enlaces entre páginas', () => {
  it('arma la cotización con apartamento, fechas y personas', () => {
    expect(enlaceCotizacion({ apartamento: 'AP002', fechaEntrada: '2026-11-28', fechaSalida: '2026-12-01', personas: 3 }))
      .toBe('/cotizacion?apartamento=AP002&fechaEntrada=2026-11-28&fechaSalida=2026-12-01&personas=3');
  });

  it('omite los datos que no vienen', () => {
    expect(enlaceDetalle('AP001', { fechaEntrada: null, personas: null })).toBe('/apartamentos/AP001');
  });

  it('lleva a la reserva las fechas de nacimiento separadas por coma', () => {
    const url = enlaceReserva({ apartamento: 'AP004', nacimientos: ['1995-01-01', '', '2016-02-10'] });
    expect(new URLSearchParams(url.split('?')[1]).get('nacimientos')).toBe('1995-01-01,2016-02-10');
  });

  it('solo acepta fechas de nacimiento con formato AAAA-MM-DD', () => {
    expect(leerNacimientos('1995-01-01, hola,2016-02-10,2016-2-1')).toEqual(['1995-01-01', '2016-02-10']);
    expect(leerNacimientos(null)).toEqual([]);
  });

  it('acepta de 1 a 20 personas', () => {
    expect(leerPersonas('3')).toBe(3);
    expect(leerPersonas('0')).toBeNull();
    expect(leerPersonas('2.5')).toBeNull();
    expect(leerPersonas('25')).toBeNull();
    expect(leerPersonas(null)).toBeNull();
  });
});
