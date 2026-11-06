// Página "/": grid de los apartamentos de ejemplo filtrados por número de personas.
// Usa TarjetaApartamento; los datos son los de la Ficha (mismos ids que el backend).
import { useEffect, useState } from 'react';
import { Cargando } from '../componentes/Cargando';
import { TarjetaApartamento } from '../componentes/TarjetaApartamento';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { listarApartamentos } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';

// Estado: personas. Valor derivado: los apartamentos donde cabe el grupo
export default function Inicio() {
  const [personas, setPersonas] = useState(2);
  const [apartamentos, setApartamentos] = useState<ApartamentoResumen[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listarApartamentos()
      .then(setApartamentos)
      .catch((e: unknown) => setError(mensajeDeError(e)))
      .finally(() => setCargando(false));
  }, []);

  const queCaben = apartamentos.filter((a) => a.activo && a.capacidadMaxima >= personas);

  return (
    <section>
      <h1 className="h3 mb-3">Apartamentos</h1>

      {cargando && <Cargando />}
      {!cargando && error && <div className="alert alert-danger">{error}</div>}

      {!cargando && !error && <div className="row g-2 align-items-center mb-3">
        <div className="col-auto">
          <label htmlFor="personas" className="col-form-label">Personas:</label>
        </div>
        <div className="col-auto">
          <input id="personas" type="number" min={1} className="form-control" value={personas}
            onChange={(e) => setPersonas(Number(e.target.value))} />
        </div>
      </div>}

      {!cargando && !error && <p className="text-body-secondary">{queCaben.length} apartamento(s) admiten {personas} persona(s).</p>}

      {/* Grid: 1 columna en móvil, 2 desde md, 3 desde lg */}
      {!cargando && !error && <div className="row g-3">
        {queCaben.map((apartamento) => (
          <div key={apartamento.identificacion} className="col-12 col-md-6 col-lg-4">
            <TarjetaApartamento apartamento={apartamento} />
          </div>
        ))}
      </div>}
    </section>
  );
}
