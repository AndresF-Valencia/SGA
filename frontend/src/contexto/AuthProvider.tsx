// Proveedor de la sesión: envuelve a App en main.tsx. Inicia sesión con authServicio, la guarda
// con sesion.ts y registra en api.ts qué hacer ante un 401. Encabezado, Login y las rutas la leen.
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { LoginRequest, LoginResponse } from '../modelos/auth';
import type { Rol } from '../modelos/usuario';
import { registrarManejadorNoAutorizado } from '../servicios/api';
import { login } from '../servicios/authServicio';
import { borrarSesion, guardarSesion, leerSesion, sesionVigente } from '../servicios/sesion';
import type { UsuarioResponse } from '../modelos/usuario';
import { ContextoAuth, type Sesion, type ValorAuth } from './contextoAuth';

// De la respuesta del backend a la sesión del frontend
function aSesion(respuesta: LoginResponse): Sesion {
  return {
    token: respuesta.token,
    rol: respuesta.usuario.rol,
    correo: respuesta.usuario.correo,
    nombre: respuesta.usuario.nombre,
    documento: respuesta.usuario.documento,
    expiraEn: respuesta.expiraEn,
  };
}

// Sesión restaurada al recargar, solo si el token sigue vigente
function sesionInicial(): Sesion | null {
  const guardada = sesionVigente();
  return guardada ? aSesion(guardada) : null;
}

// Guarda la sesión en el estado (para re-pintar) y en localStorage (para sobrevivir a la recarga)
export function AuthProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion | null>(sesionInicial);
  const [avisoSesion, setAvisoSesion] = useState<string | null>(null);

  // POST /api/auth/login; si falla, el error llega al formulario
  const iniciarSesion = useCallback(async (credenciales: LoginRequest) => {
    const respuesta = await login(credenciales);
    guardarSesion(respuesta);
    const nueva = aSesion(respuesta);
    setSesion(nueva);
    setAvisoSesion(null);
    return nueva;
  }, []);

  // Cierre voluntario de la sesión
  const cerrarSesion = useCallback(() => {
    borrarSesion();
    setSesion(null);
  }, []);

  const actualizarUsuario = useCallback((usuario: UsuarioResponse) => {
    setSesion((actual) => actual ? { ...actual, nombre: usuario.nombre, documento: usuario.documento } : actual);
    const guardada = leerSesion();
    if (guardada) guardarSesion({ ...guardada, usuario });
  }, []);

  // El interceptor de api.ts llama a esta función cuando el backend responde 401
  useEffect(() => {
    return registrarManejadorNoAutorizado(() => {
      borrarSesion();
      setSesion(null);
      setAvisoSesion('Tu sesión expiró. Ingresa de nuevo.');
    });
  }, []);

  // El objeto solo cambia cuando cambia la sesión o el aviso
  const valor = useMemo<ValorAuth>(() => ({
    sesion,
    autenticado: sesion !== null,
    avisoSesion,
    iniciarSesion,
    cerrarSesion,
    actualizarUsuario,
    tieneRol: (...roles: Rol[]) => (sesion ? roles.includes(sesion.rol) : false),
  }), [sesion, avisoSesion, iniciarSesion, cerrarSesion, actualizarUsuario]);

  return <ContextoAuth value={valor}>{children}</ContextoAuth>;
}
