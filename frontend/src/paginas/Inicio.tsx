// Página "/": lista los apartamentos de ejemplo y filtra por número de personas.
// Usa TarjetaApartamento para cada uno; en la Guía 16 los datos vendrán de GET /api/apartamentos.
import { useState } from 'react';
import { TarjetaApartamento } from '../componentes/TarjetaApartamento';
import { apartamentosEjemplo } from '../datos/apartamentosEjemplo';

// Estado: personas. Valor derivado: los apartamentos donde cabe el grupo
export default function Inicio() {
  const [personas, setPersonas] = useState(2);

  const queCaben = apartamentosEjemplo.filter((a) => a.capacidadMaxima >= personas);

  return (
    <section>
      <h1>Apartamentos</h1>

      <label>
        Personas:{' '}
        <input
          type="number"
          min={1}
          value={personas}
          onChange={(e) => setPersonas(Number(e.target.value))}
        />
      </label>

      <p>{queCaben.length} apartamento(s) admiten {personas} persona(s).</p>

      {queCaben.map((apartamento) => (
        <TarjetaApartamento key={apartamento.identificacion} apartamento={apartamento} />
      ))}
    </section>
  );
}
