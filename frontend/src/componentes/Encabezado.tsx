// Barra de navegación responsiva del Layout. Lee la sesión con useAuth() y muestra solo las
// opciones que el rol puede usar, con los mismos permisos de RutaProtegida en App.tsx.
import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router';
import { useAuth } from '../hooks/useAuth';

interface EncabezadoProps {
  nombreAlojamiento: string;
}

// Clase del enlace según si es la página actual
function claseEnlace({ isActive }: { isActive: boolean }) {
  return `nav-link ${isActive ? 'active fw-semibold' : ''}`;
}

// Marca, menú por rol y botones de sesión; el menú se despliega con estado de React
export function Encabezado({ nombreAlojamiento }: EncabezadoProps) {
  const [menuAbierto, setMenuAbierto] = useState(false);
  const navigate = useNavigate();
  const { sesion, tieneRol, cerrarSesion } = useAuth();

  // Cierra la sesión y vuelve al login
  function salir() {
    cerrarSesion();
    navigate('/login');
  }

  return (
    <header>
      <nav className="navbar navbar-expand-lg bg-body-tertiary border-bottom">
        <div className="container">
          <Link to="/" id="marca" className="navbar-brand d-flex align-items-center gap-2">
            <i className="bi bi-house-heart" aria-hidden="true"></i>
            {nombreAlojamiento}
          </Link>

          <button
            className="navbar-toggler"
            type="button"
            aria-controls="menuPrincipal"
            aria-expanded={menuAbierto}
            aria-label="Abrir menú"
            onClick={() => setMenuAbierto(!menuAbierto)}
          >
            <span className="navbar-toggler-icon"></span>
          </button>

          <div
            id="menuPrincipal"
            className={`collapse navbar-collapse ${menuAbierto ? 'show' : ''}`}
            onClick={() => setMenuAbierto(false)}
          >
            <ul className="navbar-nav me-auto">
              <li className="nav-item"><NavLink to="/" end className={claseEnlace}>Inicio</NavLink></li>
              <li className="nav-item"><NavLink to="/disponibilidad" className={claseEnlace}>Disponibilidad</NavLink></li>
              <li className="nav-item"><NavLink to="/cotizacion" className={claseEnlace}>Cotizar</NavLink></li>
              <li className="nav-item"><NavLink to="/alojamiento" className={claseEnlace}>Alojamiento</NavLink></li>
              {tieneRol('HUESPED') && (
                <>
                  <li className="nav-item"><NavLink to="/mis-reservas" className={claseEnlace}>Mis reservas</NavLink></li>
                  <li className="nav-item"><NavLink to="/reservas/nueva" className={claseEnlace}>Reservar</NavLink></li>
                </>
              )}
              {tieneRol('RECEPCIONISTA', 'ADMINISTRADOR') && (
                <>
                  <li className="nav-item"><NavLink to="/reservas" end className={claseEnlace}>Reservas</NavLink></li>
                  <li className="nav-item"><NavLink to="/reservas/nueva" className={claseEnlace}>Nueva reserva</NavLink></li>
                </>
              )}
              {tieneRol('PERSONAL_SERVICIO', 'ADMINISTRADOR') && (
                <li className="nav-item"><NavLink to="/preparacion" className={claseEnlace}>Preparación</NavLink></li>
              )}
              {tieneRol('ADMINISTRADOR') && (
                <li className="nav-item"><NavLink to="/administracion" className={claseEnlace}>Administración</NavLink></li>
              )}
            </ul>

            {sesion ? (
              <div className="d-flex align-items-center gap-2">
                <span className="navbar-text small">
                  <i className="bi bi-person-circle me-1" aria-hidden="true"></i>{sesion.nombre}
                </span>
                <Link to="/mi-cuenta" className="btn btn-outline-secondary btn-sm">Mi cuenta</Link>
                <button type="button" className="btn btn-outline-danger btn-sm" onClick={salir}>
                  <i className="bi bi-box-arrow-right me-1" aria-hidden="true"></i>Cerrar sesión
                </button>
              </div>
            ) : (
              <div className="d-flex gap-2">
                <Link to="/login" className="btn btn-primary btn-sm">Ingresar</Link>
                <Link to="/registro" className="btn btn-outline-secondary btn-sm">Registrarse</Link>
              </div>
            )}
          </div>
        </div>
      </nav>
    </header>
  );
}
