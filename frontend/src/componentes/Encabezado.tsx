// Encabezado común con la navegación a cada página registrada en App.tsx.
// Todavía no cambia según la sesión: eso llega en las Guías 16 y 18.
import { Link } from 'react-router';

interface EncabezadoProps {
  nombreAlojamiento: string;
}

// Nombre del alojamiento y enlaces internos (Link, nunca <a href>)
export function Encabezado({ nombreAlojamiento }: EncabezadoProps) {
  return (
    <header>
      <p>{nombreAlojamiento}</p>
      <nav>
        <ul>
          <li><Link to="/">Inicio</Link></li>
          <li><Link to="/disponibilidad">Buscar disponibilidad</Link></li>
          <li><Link to="/cotizacion">Cotizar</Link></li>
          <li><Link to="/mis-reservas">Mis reservas</Link></li>
          <li><Link to="/recepcion">Recepción</Link></li>
          <li><Link to="/reservas/nueva">Nueva reserva</Link></li>
          <li><Link to="/preparacion">Preparación</Link></li>
          <li><Link to="/administracion">Administración</Link></li>
          <li><Link to="/login">Ingresar</Link></li>
          <li><Link to="/registro">Registrarse</Link></li>
        </ul>
      </nav>
    </header>
  );
}
