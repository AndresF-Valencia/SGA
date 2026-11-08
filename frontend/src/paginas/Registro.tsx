// Página "/registro" (dentro de RutaPublica): crea una cuenta HUESPED con authServicio.registrar
// (POST /api/auth/registro). El backend no devuelve token, así que luego se invita al login.
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm, type Path } from 'react-hook-form';
import { Link } from 'react-router';
import { Campo } from '../componentes/Campo';
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

  // Envía el registro; un correo repetido llega como 409 del dominio
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
    <div className="row justify-content-center py-lg-4">
      <div className="col-12 col-md-10 col-lg-8">
        <div className="card shadow-sm">
          <div className="card-body p-4">
            <h1 className="h3 mb-1">Crea tu cuenta</h1>
            <p className="text-body-secondary mb-4">Con tu cuenta puedes reservar en línea y consultar tus reservas.</p>

            <form onSubmit={handleSubmit(alEnviar)} noValidate>
              <div className="row">
                <div className="col-12 col-md-6">
                  <Campo id="nombre" etiqueta="Nombre completo" autoComplete="name"
                    error={errors.nombre?.message} {...register('nombre')} />
                </div>
                <div className="col-12 col-md-6">
                  <Campo id="correo" etiqueta="Correo electrónico" type="email" autoComplete="email"
                    error={errors.correo?.message} {...register('correo')} />
                </div>
                <div className="col-12 col-md-6">
                  <Campo id="contrasena" etiqueta="Contraseña" type="password" autoComplete="new-password"
                    error={errors.contrasena?.message} {...register('contrasena')} />
                </div>
                <div className="col-12 col-md-6">
                  <Campo id="confirmacion" etiqueta="Confirmar contraseña" type="password" autoComplete="new-password"
                    error={errors.confirmacion?.message} {...register('confirmacion')} />
                </div>
                <div className="col-12 col-md-4">
                  <Campo id="telefono" etiqueta="Teléfono" type="tel" autoComplete="tel"
                    error={errors.telefono?.message} {...register('telefono')} />
                </div>
                <div className="col-12 col-md-4">
                  <Campo id="documento" etiqueta="Documento"
                    error={errors.documento?.message} {...register('documento')} />
                </div>
                <div className="col-12 col-md-4">
                  <Campo id="fechaNacimiento" etiqueta="Fecha de nacimiento" type="date"
                    error={errors.fechaNacimiento?.message} {...register('fechaNacimiento')} />
                </div>
              </div>

              <div className="d-grid">
                <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                  {isSubmitting && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>}
                  {isSubmitting ? 'Registrando…' : 'Registrarse'}
                </button>
              </div>

              {errors.root?.servidor && (
                <div className="alert alert-danger mt-3 mb-0">{errors.root.servidor.message}</div>
              )}
              {correoCreado && (
                <div className="alert alert-success mt-3 mb-0">
                  Cuenta {correoCreado} creada. <Link to="/login">Ingrese aquí</Link>.
                </div>
              )}
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
