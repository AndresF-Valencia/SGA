// Ruta de diseño que protege a sus rutas hijas en App.tsx por sesión y rol. Es solo ayuda de
// navegación: la seguridad real la aplica SecurityConfig del backend (401/403).
import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import type { Rol } from '../modelos/usuario';

interface RutaProtegidaProps {
  roles?: Rol[]; // si no se indica, basta con haber iniciado sesión
}

// Sin sesión → /login (recordando a dónde iba); rol insuficiente → /no-autorizado
export function RutaProtegida({ roles }: RutaProtegidaProps) {
  const { autenticado, tieneRol } = useAuth();
  const ubicacion = useLocation();

  if (!autenticado) {
    return <Navigate to="/login" replace state={{ desde: ubicacion.pathname }} />;
  }
  if (roles && !tieneRol(...roles)) {
    return <Navigate to="/no-autorizado" replace />;
  }
  return <Outlet />;
}
