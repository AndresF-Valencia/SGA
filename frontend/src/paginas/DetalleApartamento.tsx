// Página "/apartamentos/:identificacion": detalle de un apartamento de ejemplo.
// El parámetro de la URL es la misma identificación que usa el backend (AP001...).
import { Link, useParams } from 'react-router';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';

// Busca el apartamento por el parámetro de la ruta; si no existe, lo dice
export default function DetalleApartamento() {
  const { identificacion } = useParams();
  const apartamento = apartamentosEjemplo.find((a) => a.identificacion === identificacion);

  if (!apartamento) {
    return <p>No existe el apartamento {identificacion}.</p>;
  }

  return (
    <section>
      <h1>{apartamento.nombre}</h1>
      <p>Identificación: {apartamento.identificacion}</p>
      <p>Dormitorios: {apartamento.dormitorios}</p>
      <p>Capacidad: {apartamento.capacidadMaxima} personas</p>
      <p>Mascotas: hasta {apartamento.limiteMascotas}</p>
      <ul>
        {apartamento.caracteristicas.map((c) => (
          <li key={c}>{c}</li>
        ))}
      </ul>
      <Link to="/cotizacion">Cotizar una estancia</Link> · <Link to="/">Volver</Link>
    </section>
  );
}
