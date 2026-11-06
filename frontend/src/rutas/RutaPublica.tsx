// Ruta de diseño para login y registro: solo sin sesión. En cuanto el AuthProvider tiene sesión,
// redirige a la página que se intentaba abrir o a la inicial del rol (rutaInicial.ts).
import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import { rutaInicial } from './rutaInicial';

// Con sesión redirige; sin sesión pinta la página hija
export function RutaPublica() {
  const { sesion } = useAuth();
  const ubicacion = useLocation();

  if (sesion) {
    const desde = (ubicacion.state as { desde?: string } | null)?.desde;
    return <Navigate to={desde ?? rutaInicial(sesion.rol)} replace />;
  }
  return <Outlet />;
}
