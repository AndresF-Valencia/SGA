// Las reglas de los formularios repiten las del backend: se prueban los casos que el
// usuario puede escribir mal.
import { describe, expect, it } from 'vitest';
import { esquemaCotizacion } from '../esquemas/cotizacion';
import { esquemaRegistro } from '../esquemas/registro';

// "AAAA-MM-DD" a n días de hoy (hora local, igual que hoyISO)
function enDias(n: number): string {
  const f = new Date();
  f.setDate(f.getDate() + n);
  return `${f.getFullYear()}-${String(f.getMonth() + 1).padStart(2, '0')}-${String(f.getDate()).padStart(2, '0')}`;
}

// Primer mensaje de error de un campo, o undefined si el campo está bien
function errorDe(resultado: { success: boolean; error?: { issues: { path: PropertyKey[]; message: string }[] } }, campo: string) {
  return resultado.error?.issues.find((i) => i.path.join('.') === campo)?.message;
}

describe('esquemaCotizacion', () => {
  const valida = {
    identificacionApartamento: 'AP001',
    fechaEntrada: enDias(5),
    fechaSalida: enDias(8),
    ocupantes: [{ fechaNacimiento: '1995-01-01' }],
  };

  it('acepta una cotización correcta', () => {
    expect(esquemaCotizacion.safeParse(valida).success).toBe(true);
  });

  it('rechaza una salida igual o anterior a la entrada', () => {
    const r = esquemaCotizacion.safeParse({ ...valida, fechaSalida: valida.fechaEntrada });
    expect(errorDe(r, 'fechaSalida')).toBe('La fecha de salida debe ser posterior a la de entrada');
  });

  it('rechaza una entrada en el pasado', () => {
    const r = esquemaCotizacion.safeParse({ ...valida, fechaEntrada: enDias(-1) });
    expect(errorDe(r, 'fechaEntrada')).toBe('La fecha de entrada debe ser en el futuro');
  });

  it('exige al menos una persona y fechas de nacimiento no futuras', () => {
    expect(errorDe(esquemaCotizacion.safeParse({ ...valida, ocupantes: [] }), 'ocupantes')).toBe('Agrega al menos una persona');
    const r = esquemaCotizacion.safeParse({ ...valida, ocupantes: [{ fechaNacimiento: enDias(3) }] });
    expect(errorDe(r, 'ocupantes.0.fechaNacimiento')).toBe('La fecha de nacimiento no puede ser futura');
  });
});

describe('esquemaRegistro', () => {
  const valido = {
    nombre: 'Laura Gómez',
    correo: 'laura@correo.co',
    contrasena: 'Sanrocco123',
    confirmacion: 'Sanrocco123',
    telefono: '3001234567',
    documento: '1094000001',
    fechaNacimiento: '1995-01-01',
  };

  it('acepta un registro correcto', () => {
    expect(esquemaRegistro.safeParse(valido).success).toBe(true);
  });

  it('pide mayúscula y número en la contraseña, igual que el backend', () => {
    expect(errorDe(esquemaRegistro.safeParse({ ...valido, contrasena: 'sanrocco123', confirmacion: 'sanrocco123' }), 'contrasena'))
      .toBe('Debe tener al menos una mayúscula');
    expect(errorDe(esquemaRegistro.safeParse({ ...valido, contrasena: 'Sanrocco', confirmacion: 'Sanrocco' }), 'contrasena'))
      .toBe('Debe tener al menos un número');
  });

  it('avisa cuando la confirmación no coincide', () => {
    expect(errorDe(esquemaRegistro.safeParse({ ...valido, confirmacion: 'Otra1234' }), 'confirmacion')).toBe('Las contraseñas no coinciden');
  });
});
