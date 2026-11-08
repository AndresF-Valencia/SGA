// Página para cualquier URL sin ruta (path="*" en App.tsx): una puerta tapiada y dos salidas.
import { Link } from 'react-router';

export default function NoEncontrada() {
  return (
    <section className="no-encontrada">
      <span className="puerta-tapiada" aria-hidden="true"></span>
      <h1 className="h2">Esta puerta no lleva a ningún lado</h1>
      <p className="text-body-secondary">La página que buscas no existe o cambió de dirección.</p>
      <div className="d-flex flex-wrap justify-content-center gap-2">
        <Link to="/" className="btn btn-primary">Ir al inicio</Link>
        <Link to="/disponibilidad" className="btn btn-outline-primary">Buscar fechas</Link>
      </div>
    </section>
  );
}
