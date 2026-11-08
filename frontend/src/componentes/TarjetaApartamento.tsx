// Tarjeta de un apartamento en el grid de Inicio; enlaza a DetalleApartamento usando la
// identificación del negocio en la URL (/apartamentos/AP001). El borde superior lleva el
// color de su puerta (utilidades/identidad.ts).
import type { CSSProperties } from 'react';
import { Link } from 'react-router';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { colorPuerta, miniatura } from '../utilidades/identidad';

interface TarjetaApartamentoProps {
  apartamento: ApartamentoResumen;
}

export function TarjetaApartamento({ apartamento }: TarjetaApartamentoProps) {
  const dormitorios = apartamento.dormitorios === 1 ? '1 dormitorio' : `${apartamento.dormitorios} dormitorios`;
  return (
    <article className="card h-100 shadow-sm tarjeta-apto"
      style={{ '--puerta': colorPuerta(apartamento.identificacion) } as CSSProperties}>
      {apartamento.imagenes[0] && (
        <img src={miniatura(apartamento.imagenes[0])} alt="" className="tarjeta-apto-foto" loading="lazy" />
      )}
      <div className="card-body">
        <h3 className="h4 card-title mb-1">{apartamento.nombre}</h3>
        <p className="text-body-secondary mb-3">
          {dormitorios}, hasta {apartamento.capacidadMaxima} personas
          {apartamento.limiteMascotas > 0 && <>, admite mascotas</>}
        </p>
        <p className="mb-0 small text-body-secondary">{apartamento.caracteristicas.slice(0, 5).join(', ')}</p>
      </div>
      <div className="card-footer bg-transparent border-0 pt-0 pb-3">
        <Link to={`/apartamentos/${apartamento.identificacion}`} className="btn btn-outline-primary btn-sm">
          Ver apartamento
        </Link>
      </div>
    </article>
  );
}
