// Página para cualquier URL sin ruta (path="*" en App.tsx).
import { Link } from 'react-router';

// Mensaje y enlace de vuelta al inicio
export default function NoEncontrada() {
  return (
    <section>
      <h1>Página no encontrada</h1>
      <Link to="/">Ir al inicio</Link>
    </section>
  );
}
