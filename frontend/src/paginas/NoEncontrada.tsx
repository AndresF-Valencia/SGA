// Página para cualquier URL sin ruta (path="*" en App.tsx).
import { Link } from 'react-router';

// Mensaje y enlace de vuelta al inicio
export default function NoEncontrada() {
  return (
    <section className="text-center py-5">
      <h1 className="display-6">Página no encontrada</h1>
      <Link to="/" className="btn btn-primary mt-3">Ir al inicio</Link>
    </section>
  );
}
