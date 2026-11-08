// Traducción de los errores del backend a mensajes y a errores por campo del formulario.
import { AxiosError, AxiosHeaders } from 'axios';
import { describe, expect, it } from 'vitest';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';

// Error de Axios con la respuesta que mandaría el backend
function errorHttp(status: number, data: unknown) {
  const config = { headers: new AxiosHeaders() };
  return new AxiosError('fallo', 'ERR', config, null, { status, statusText: '', headers: {}, config, data });
}

describe('mensajeDeError', () => {
  it('usa el mensaje de negocio del backend', () => {
    expect(mensajeDeError(errorHttp(409, { message: 'El apartamento ya está reservado en esas fechas' })))
      .toBe('El apartamento ya está reservado en esas fechas');
  });

  it('explica cuando el servidor no responde', () => {
    expect(mensajeDeError(new AxiosError('Network Error'))).toMatch(/No pudimos conectar/);
  });

  it('da un mensaje propio para un 403 sin texto', () => {
    expect(mensajeDeError(errorHttp(403, {}))).toBe('Tu cuenta no tiene permiso para hacer esto.');
  });
});

describe('erroresDeCampo', () => {
  it('convierte la notación del backend a la de React Hook Form', () => {
    const error = errorHttp(400, { detallesValidacion: ['ocupantes[1].documento: no puede estar vacío', 'fechaSalida: debe ser futura'] });
    expect(erroresDeCampo(error)).toEqual({
      'ocupantes.1.documento': 'no puede estar vacío',
      fechaSalida: 'debe ser futura',
    });
  });
});
