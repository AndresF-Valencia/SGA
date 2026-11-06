// Tarjeta de un apartamento en el listado de Inicio; enlaza a DetalleApartamento
// usando la identificación del negocio en la URL (/apartamentos/AP001).
import { Link } from 'react-router';
import type { ApartamentoResumen } from '../modelos/apartamento';

interface TarjetaApartamentoProps {
  apartamento: ApartamentoResumen;
}

// Pinta nombre, dormitorios y capacidad, con el enlace al detalle
export function TarjetaApartamento({ apartamento }: TarjetaApartamentoProps) {
  return (
    <article className="tarjeta">
      <h3>{apartamento.nombre}</h3>
      <p>
        {apartamento.dormitorios} dormitorio(s) · hasta {apartamento.capacidadMaxima} personas
      </p>
      <Link to={`/apartamentos/${apartamento.identificacion}`}>Ver detalle</Link>
    </article>
  );
}
