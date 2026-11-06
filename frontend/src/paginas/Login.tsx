// Página "/login": valida con esquemaLogin, llama a authServicio.login (POST /api/auth/login)
// y guarda la sesión con sesion.ts para que los servicios protegidos envíen el token.
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router';
import { esquemaLogin, type FormularioLogin } from '../esquemas/login';
import { login } from '../servicios/authServicio';
import { guardarSesion } from '../servicios/sesion';
import { estadoHttp, mensajeDeError } from '../utilidades/errores';

// Formulario de ingreso con errores por campo y error general del servidor
export default function Login() {
  const navigate = useNavigate();
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

  // Solo se llama si el esquema aprobó los datos; 401 = credenciales incorrectas
  async function alEnviar(datos: FormularioLogin) {
    try {
      const sesion = await login(datos);
      guardarSesion(sesion);
      navigate('/');
    } catch (error) {
      setError('root.servidor', {
        message: estadoHttp(error) === 401 ? 'Correo o contraseña incorrectos' : mensajeDeError(error),
      });
    }
  }

  return (
    <section>
      <h1>Ingresar</h1>

      <form onSubmit={handleSubmit(alEnviar)} noValidate>
        <div>
          <label htmlFor="correo">Correo electrónico</label>
          <input id="correo" type="email" autoComplete="username" {...register('correo')} />
          {errors.correo && <p className="error">{errors.correo.message}</p>}
        </div>

        <div>
          <label htmlFor="contrasena">Contraseña</label>
          <input id="contrasena" type="password" autoComplete="current-password" {...register('contrasena')} />
          {errors.contrasena && <p className="error">{errors.contrasena.message}</p>}
        </div>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Ingresando…' : 'Ingresar'}
        </button>

        {errors.root?.servidor && <p className="error">{errors.root.servidor.message}</p>}
      </form>

      <p>¿No tiene cuenta? <Link to="/registro">Regístrese</Link></p>
    </section>
  );
}
