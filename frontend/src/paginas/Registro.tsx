// Página "/registro": crea una cuenta HUESPED con authServicio.registrar
// (POST /api/auth/registro). El backend no devuelve token, así que luego se envía al login.
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm, type Path } from 'react-hook-form';
import { Link } from 'react-router';
import { esquemaRegistro, type FormularioRegistro } from '../esquemas/registro';
import type { RegistroUsuarioRequest } from '../modelos/usuario';
import { registrar } from '../servicios/authServicio';
import { erroresDeCampo, mensajeDeError } from '../utilidades/errores';

// Del formulario al DTO: la confirmación de contraseña no viaja al backend
function aSolicitud({ confirmacion: _confirmacion, ...datos }: FormularioRegistro): RegistroUsuarioRequest {
  return datos;
}

// Formulario de registro con errores de Zod, de campo (400) y de negocio (409)
export default function Registro() {
  const [correoCreado, setCorreoCreado] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioRegistro>({
    resolver: zodResolver(esquemaRegistro),
    mode: 'onTouched',
    defaultValues: {
      nombre: '', correo: '', contrasena: '', confirmacion: '',
      telefono: '', documento: '', fechaNacimiento: '',
    },
  });

  // Envía el registro; un correo o documento repetido llega como 409 del dominio
  async function alEnviar(datos: FormularioRegistro) {
    setCorreoCreado(null);
    try {
      const usuario = await registrar(aSolicitud(datos));
      setCorreoCreado(usuario.correo);
      reset();
    } catch (error) {
      for (const [campo, mensaje] of Object.entries(erroresDeCampo(error))) {
        setError(campo as Path<FormularioRegistro>, { message: mensaje });
      }
      setError('root.servidor', { message: mensajeDeError(error) });
    }
  }

  return (
    <section>
      <h1>Registrarse</h1>
      <p>El registro público crea usuarios con rol Huésped (7.2).</p>

      <form onSubmit={handleSubmit(alEnviar)} noValidate>
        <div>
          <label htmlFor="nombre">Nombre completo</label>
          <input id="nombre" autoComplete="name" {...register('nombre')} />
          {errors.nombre && <p className="error">{errors.nombre.message}</p>}
        </div>

        <div>
          <label htmlFor="correo">Correo electrónico</label>
          <input id="correo" type="email" autoComplete="email" {...register('correo')} />
          {errors.correo && <p className="error">{errors.correo.message}</p>}
        </div>

        <div>
          <label htmlFor="contrasena">Contraseña</label>
          <input id="contrasena" type="password" autoComplete="new-password" {...register('contrasena')} />
          {errors.contrasena && <p className="error">{errors.contrasena.message}</p>}
        </div>

        <div>
          <label htmlFor="confirmacion">Confirmar contraseña</label>
          <input id="confirmacion" type="password" autoComplete="new-password" {...register('confirmacion')} />
          {errors.confirmacion && <p className="error">{errors.confirmacion.message}</p>}
        </div>

        <div>
          <label htmlFor="telefono">Teléfono</label>
          <input id="telefono" type="tel" autoComplete="tel" {...register('telefono')} />
          {errors.telefono && <p className="error">{errors.telefono.message}</p>}
        </div>

        <div>
          <label htmlFor="documento">Documento</label>
          <input id="documento" {...register('documento')} />
          {errors.documento && <p className="error">{errors.documento.message}</p>}
        </div>

        <div>
          <label htmlFor="fechaNacimiento">Fecha de nacimiento</label>
          <input id="fechaNacimiento" type="date" {...register('fechaNacimiento')} />
          {errors.fechaNacimiento && <p className="error">{errors.fechaNacimiento.message}</p>}
        </div>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Registrando…' : 'Registrarse'}
        </button>

        {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
        {correoCreado && (
          <p className="exito">
            Cuenta {correoCreado} creada. <Link to="/login">Ingrese aquí</Link>.
          </p>
        )}
      </form>
    </section>
  );
}
