// Barra de navegación responsiva del Layout. Lee la sesión con useAuth() y muestra solo las
// opciones que el rol puede usar, con los mismos permisos de RutaProtegida en App.tsx.
import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import { Puerta } from './Puerta';

interface EncabezadoProps {
  nombreAlojamiento: string;
}

// Clase del enlace según si es la página actual
function claseEnlace({ isActive }: { isActive: boolean }) {
  return `nav-link text-nowrap ${isActive ? 'active' : ''}`;
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
    <header className="encabezado">
      <nav className="navbar navbar-expand-xl py-2">
        <div className="container">
          <Link to="/" id="marca" className="navbar-brand marca" aria-label={nombreAlojamiento}>
            <span className="marca-puertas">
              {['AP001', 'AP002', 'AP003', 'AP004', 'AP005'].map((id) => <Puerta key={id} identificacion={id} />)}
            </span>
            <span>
              <span className="marca-nombre">San Rocco</span>
              <span className="marca-lugar">Apartamentos en Salento</span>
            </span>
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
            <ul className="navbar-nav me-auto ms-xl-3 gap-xl-1">
              <li className="nav-item"><NavLink to="/disponibilidad" className={claseEnlace}>Buscar fechas</NavLink></li>
              <li className="nav-item"><NavLink to="/cotizacion" className={claseEnlace}>Cotizar</NavLink></li>
              <li className="nav-item"><NavLink to="/alojamiento" className={claseEnlace}>El alojamiento</NavLink></li>
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
              <div className="d-flex align-items-center gap-2 mt-2 mt-xl-0">
                <Link to="/mi-cuenta" className="btn btn-outline-secondary btn-sm text-nowrap" title="Mi cuenta">
                  <i className="bi bi-person-circle me-1" aria-hidden="true"></i>{sesion.nombre.split(' ')[0]}
                </Link>
                <button type="button" className="btn btn-outline-secondary btn-sm text-nowrap" onClick={salir}>
                  <i className="bi bi-box-arrow-right me-1" aria-hidden="true"></i>Salir
                </button>
              </div>
            ) : (
              <div className="d-flex gap-2 mt-2 mt-xl-0">
                <Link to="/login" className="btn btn-outline-secondary btn-sm">Ingresar</Link>
                <Link to="/registro" className="btn btn-primary btn-sm">Crear cuenta</Link>
              </div>
            )}
          </div>
        </div>
      </nav>
    </header>
  );
}
