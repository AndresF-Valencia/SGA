import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { Campo } from '../componentes/Campo';
import { esquemaSolicitarRecuperacion, esquemaRestablecerContrasena, type FormularioSolicitarRecuperacion, type FormularioRestablecerContrasena } from '../esquemas/recuperacion';
import { solicitarRecuperacion, restablecerContrasena } from '../servicios/authServicio';
import { mensajeDeError } from '../utilidades/errores';
import { notificarError, notificarExito } from '../utilidades/notificaciones';

export default function RecuperarContrasena() {
  const solicitar = useForm<FormularioSolicitarRecuperacion>({ resolver: zodResolver(esquemaSolicitarRecuperacion), mode: 'onTouched' });
  const formRestablecer = useForm<FormularioRestablecerContrasena>({ resolver: zodResolver(esquemaRestablecerContrasena), mode: 'onTouched' });

  async function enviarSolicitud(datos: FormularioSolicitarRecuperacion) {
    try {
      await solicitarRecuperacion(datos.correo);
      await notificarExito('Si el correo existe, recibirá un código de recuperación.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function enviarRestablecimiento(datos: FormularioRestablecerContrasena) {
    try {
      await restablecerContrasena(datos);
      await notificarExito('Contraseña actualizada. Ya puede iniciar sesión.');
      formRestablecer.reset();
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  return (
    <section className="row g-4 justify-content-center">
      <div className="col-12 col-lg-5">
        <form className="card card-body" onSubmit={solicitar.handleSubmit(enviarSolicitud)} noValidate>
          <h1 className="h4">Solicitar código</h1>
          <Campo id="correo-solicitud" etiqueta="Correo" type="email" error={solicitar.formState.errors.correo?.message} {...solicitar.register('correo')} />
          <button className="btn btn-primary" disabled={solicitar.formState.isSubmitting}>Enviar código</button>
        </form>
      </div>
      <div className="col-12 col-lg-5">
        <form className="card card-body" onSubmit={formRestablecer.handleSubmit(enviarRestablecimiento)} noValidate>
          <h2 className="h4">Restablecer contraseña</h2>
          <Campo id="correo-restablecer" etiqueta="Correo" type="email" error={formRestablecer.formState.errors.correo?.message} {...formRestablecer.register('correo')} />
          <Campo id="codigo" etiqueta="Código de 6 dígitos" inputMode="numeric" error={formRestablecer.formState.errors.codigo?.message} {...formRestablecer.register('codigo')} />
          <Campo id="nuevaContrasena" etiqueta="Nueva contraseña" type="password" error={formRestablecer.formState.errors.nuevaContrasena?.message} {...formRestablecer.register('nuevaContrasena')} />
          <button className="btn btn-primary" disabled={formRestablecer.formState.isSubmitting}>Cambiar contraseña</button>
          <Link to="/login" className="text-center mt-3">Volver al ingreso</Link>
        </form>
      </div>
    </section>
  );
}