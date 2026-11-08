// Página "/login" (dentro de RutaPublica): valida con esquemaLogin e inicia sesión con
// useAuth().iniciarSesion (POST /api/auth/login). Al haber sesión, RutaPublica redirige.
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { Campo } from '../componentes/Campo';
import { esquemaLogin, type FormularioLogin } from '../esquemas/login';
import { useAuth } from '../hooks/useAuth';
import { estadoHttp, mensajeDeError } from '../utilidades/errores';

// Formulario de ingreso con aviso de sesión vencida y error general del servidor
export default function Login() {
  const { iniciarSesion, avisoSesion } = useAuth();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioLogin>({
    resolver: zodResolver(esquemaLogin),
    mode: 'onTouched',
    defaultValues: { correo: '', contrasena: '' },
  });

  // Solo se llama si el esquema aprobó los datos; aquí no se navega (lo hace RutaPublica)
  async function alEnviar(datos: FormularioLogin) {
    try {
      await iniciarSesion(datos);
    } catch (error) {
      setError('root.servidor', {
        message: estadoHttp(error) === 401 ? 'Correo o contraseña incorrectos' : mensajeDeError(error),
      });
    }
  }

  return (
    <div className="row justify-content-center py-lg-4">
      <div className="col-12 col-sm-10 col-md-7 col-lg-5">
        <div className="card shadow-sm">
          <div className="card-body p-4">
            <h1 className="h3 mb-1">Ingresa a tu cuenta</h1>
            <p className="text-body-secondary mb-4">Para ver tus reservas o reservar en línea.</p>
            {avisoSesion && <div className="alert alert-warning">{avisoSesion}</div>}

            <form onSubmit={handleSubmit(alEnviar)} noValidate>
              <Campo
                id="correo"
                etiqueta="Correo electrónico"
                type="email"
                autoComplete="username"
                placeholder="correo@ejemplo.com"
                error={errors.correo?.message}
                {...register('correo')}
              />

              <Campo
                id="contrasena"
                etiqueta="Contraseña"
                type="password"
                autoComplete="current-password"
                error={errors.contrasena?.message}
                {...register('contrasena')}
              />

              <div className="mb-3 text-end">
                <Link to="/recuperar-contrasena" className="small">¿Olvidó su contraseña?</Link>
              </div>

              <div className="d-grid">
                <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                  {isSubmitting && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>}
                  {isSubmitting ? 'Ingresando…' : 'Ingresar'}
                </button>
              </div>

              {errors.root?.servidor && (
                <div className="alert alert-danger mt-3 mb-0">{errors.root.servidor.message}</div>
              )}
            </form>
          </div>
          <div className="card-footer text-center small">
            ¿No tiene cuenta? <Link to="/registro">Regístrese</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
