import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router';
import { Campo } from '../componentes/Campo';
import { confirmarAccion, notificarError, notificarExito } from '../utilidades/notificaciones';
import { esquemaCambioContrasena, esquemaPerfil, type FormularioCambioContrasena, type FormularioPerfil } from '../esquemas/cuenta';
import type { UsuarioResponse } from '../modelos/usuario';
import { actualizarMiPerfil, cambiarMiContrasena, eliminarMiCuenta, obtenerMiPerfil } from '../servicios/usuarioServicio';
import { mensajeDeError } from '../utilidades/errores';
import { useAuth } from '../hooks/useAuth';

export default function MiCuenta() {
  const { actualizarUsuario, cerrarSesion } = useAuth();
  const navigate = useNavigate();
  const [perfil, setPerfil] = useState<UsuarioResponse | null>(null);
  const formPerfil = useForm<FormularioPerfil>({ resolver: zodResolver(esquemaPerfil), mode: 'onTouched' });
  const formContrasena = useForm<FormularioCambioContrasena>({ resolver: zodResolver(esquemaCambioContrasena), mode: 'onTouched' });

  useEffect(() => {
    obtenerMiPerfil()
      .then((datos) => {
        setPerfil(datos);
        formPerfil.reset({ nombre: datos.nombre, telefono: datos.telefono, fotoPerfil: datos.fotoPerfil ?? '' });
      })
      .catch((e: unknown) => void notificarError(mensajeDeError(e)));
  }, [formPerfil]);

  async function guardarPerfil(datos: FormularioPerfil) {
    try {
      const actualizado = await actualizarMiPerfil(datos);
      setPerfil(actualizado);
      actualizarUsuario(actualizado);
      await notificarExito('Perfil actualizado.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function cambiarContrasena(datos: FormularioCambioContrasena) {
    try {
      await cambiarMiContrasena(datos);
      formContrasena.reset();
      await notificarExito('Contraseña actualizada.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  async function eliminarCuenta() {
    if (!await confirmarAccion('¿Eliminar la cuenta?', 'Esta acción desactivará su cuenta y cerrará la sesión.')) return;
    try {
      await eliminarMiCuenta();
      cerrarSesion();
      navigate('/');
      await notificarExito('Cuenta eliminada.');
    } catch (e: unknown) {
      await notificarError(mensajeDeError(e));
    }
  }

  if (!perfil) return <div className="text-center py-5">Cargando perfil…</div>;

  return (
    <section>
      <h1 className="h3 mb-4">Mi cuenta</h1>
      <div className="row g-4">
        <div className="col-12 col-lg-7">
          <form className="card card-body" onSubmit={formPerfil.handleSubmit(guardarPerfil)} noValidate>
            <h2 className="h5">Perfil</h2>
            <p className="text-body-secondary small">Correo: {perfil.correo} · Documento: {perfil.documento}</p>
            <Campo id="nombre" etiqueta="Nombre" error={formPerfil.formState.errors.nombre?.message} {...formPerfil.register('nombre')} />
            <Campo id="telefono" etiqueta="Teléfono" error={formPerfil.formState.errors.telefono?.message} {...formPerfil.register('telefono')} />
            <Campo id="fotoPerfil" etiqueta="URL de foto (opcional)" error={formPerfil.formState.errors.fotoPerfil?.message} {...formPerfil.register('fotoPerfil')} />
            <button className="btn btn-primary" disabled={formPerfil.formState.isSubmitting}>Guardar perfil</button>
          </form>
        </div>
        <div className="col-12 col-lg-5">
          <form className="card card-body mb-4" onSubmit={formContrasena.handleSubmit(cambiarContrasena)} noValidate>
            <h2 className="h5">Cambiar contraseña</h2>
            <Campo id="contrasenaActual" etiqueta="Contraseña actual" type="password" error={formContrasena.formState.errors.contrasenaActual?.message} {...formContrasena.register('contrasenaActual')} />
            <Campo id="nuevaContrasena" etiqueta="Nueva contraseña" type="password" error={formContrasena.formState.errors.nuevaContrasena?.message} {...formContrasena.register('nuevaContrasena')} />
            <button className="btn btn-outline-primary" disabled={formContrasena.formState.isSubmitting}>Cambiar contraseña</button>
          </form>
          <button type="button" className="btn btn-outline-danger w-100" onClick={() => void eliminarCuenta()}>
            Eliminar mi cuenta
          </button>
        </div>
      </div>
    </section>
  );
}