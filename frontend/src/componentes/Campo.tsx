// Campo de formulario reutilizable (etiqueta + input + error con Bootstrap).
// Lo usan todos los formularios (Login, Registro, NuevaReserva, Cotizacion, BuscarDisponibilidad)
// con {...register('campo')} de React Hook Form; el error viene de Zod o del 400 del backend.
import type { ComponentProps } from 'react';

// Todo lo que acepta un <input>, más la etiqueta y el mensaje de error
interface CampoProps extends ComponentProps<'input'> {
  etiqueta: string;
  error?: string;
}

// Pinta el campo en rojo y muestra el mensaje si hay error. React 19: ref llega como prop
export function Campo({ etiqueta, error, id, className = '', ...propsDelInput }: CampoProps) {
  return (
    <div className="mb-3">
      <label htmlFor={id} className="form-label">{etiqueta}</label>
      <input
        id={id}
        className={`form-control ${error ? 'is-invalid' : ''} ${className}`}
        aria-invalid={error ? true : undefined}
        {...propsDelInput}
      />
      <div className="invalid-feedback">{error}</div>
    </div>
  );
}
