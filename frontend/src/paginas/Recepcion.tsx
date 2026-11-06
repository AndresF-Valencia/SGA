// Página provisional "/recepcion" (RECEPCIONISTA). Consumirá GET /api/reservas y las
// acciones confirmar, llegada, salida y folio. Ya enlaza a la nueva reserva (Guía 15).
import { Link } from 'react-router';

// Título y acceso a crear una reserva por canal DIRECTO
export default function Recepcion() {
  return (
    <section>
      <h1>Recepción</h1>
      <Link to="/reservas/nueva">Nueva reserva</Link>
    </section>
  );
}
