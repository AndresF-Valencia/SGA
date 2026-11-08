// Tarjeta de un apartamento en el grid de Inicio; enlaza a DetalleApartamento usando la
// identificación del negocio en la URL (/apartamentos/AP001).
import { Link } from 'react-router';
import type { ApartamentoResumen } from '../modelos/apartamento';

interface TarjetaApartamentoProps {
  apartamento: ApartamentoResumen;
}

// Pinta nombre, dormitorios y capacidad, con el enlace al detalle
export function TarjetaApartamento({ apartamento }: TarjetaApartamentoProps) {
  return (
    <article className="card h-100 shadow-sm">
      <div className="card-body">
        <h3 className="h5 card-title">{apartamento.nombre}</h3>
        <p className="card-text text-body-secondary mb-0">
          <i className="bi bi-door-closed me-1" aria-hidden="true"></i>
          {apartamento.dormitorios} dormitorio(s)
          <span className="mx-2">·</span>
          <i className="bi bi-people me-1" aria-hidden="true"></i>
          hasta {apartamento.capacidadMaxima} personas
        </p>
      </div>
      <div className="card-footer bg-transparent">
        <Link to={`/apartamentos/${apartamento.identificacion}`} className="btn btn-outline-primary btn-sm">
          Ver detalle
        </Link>
      </div>
    </article>
  );
}
