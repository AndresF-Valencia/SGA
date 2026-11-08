// Página "/": presenta San Rocco con su fachada (una puerta por apartamento, que se abre y deja
// ver el interior), un buscador
// que lleva a /disponibilidad con los criterios en la URL, y la lista de apartamentos.
import { useEffect, useState, type CSSProperties, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import { Cargando } from '../componentes/Cargando';
import { Puerta } from '../componentes/Puerta';
import type { ApartamentoResumen } from '../modelos/apartamento';
import { escucharCambiosApartamentos, listarApartamentos } from '../servicios/apartamentoServicio';
import { mensajeDeError } from '../utilidades/errores';
import { FOTO_PORTADA, FOTO_SALENTO_CALLE, FOTO_SALENTO_MONTANA, colorPuerta, miniatura } from '../utilidades/identidad';

// "AAAA-MM-DD" de hoy más n días, en hora local
function dentroDe(dias: number): string {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() + dias);
  return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}-${String(fecha.getDate()).padStart(2, '0')}`;
}

export default function Inicio() {
  const navigate = useNavigate();
  const [apartamentos, setApartamentos] = useState<ApartamentoResumen[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [entrada, setEntrada] = useState(dentroDe(7));
  const [salida, setSalida] = useState(dentroDe(10));
  const [personas, setPersonas] = useState(2);

  useEffect(() => {
    const cargar = () => {
      setCargando(true);
      setError(null);
      listarApartamentos().then(setApartamentos).catch((e: unknown) => setError(mensajeDeError(e))).finally(() => setCargando(false));
    };
    cargar();
    return escucharCambiosApartamentos(cargar);
  }, []);

  const activos = apartamentos.filter((a) => a.activo);

  function buscar(evento: FormEvent) {
    evento.preventDefault();
    const criterios = new URLSearchParams({ fechaEntrada: entrada, fechaSalida: salida, personas: String(personas), mascotas: '0' });
    navigate(`/disponibilidad?${criterios.toString()}`);
  }

  return (
    <>
      <section className="portada" style={{ backgroundImage: `url(${FOTO_PORTADA})` }}>
        <div className="container portada-contenido">
          <div className="portada-rejilla">
            <div>
              <h1 className="portada-titulo">Una puerta propia en Salento</h1>
              <p className="portada-texto">
                Cinco apartamentos con cocina, a pocas cuadras de la plaza y a veinte minutos de las palmas del Valle del Cocora.
              </p>
            </div>

            {/* La fachada: cada puerta es un apartamento; al pasar el mouse se abre y muestra el interior */}
            {activos.length > 0 && (
              <nav className="fachada" aria-label="Los apartamentos">
                {activos.map((a) => (
                  <Link key={a.identificacion} to={`/apartamentos/${a.identificacion}`} className="fachada-puerta"
                    style={{ '--puerta': colorPuerta(a.identificacion) } as CSSProperties}>
                    <span className="fachada-vano" aria-hidden="true">
                      {a.imagenes[0] && <img src={miniatura(a.imagenes[0], 240)} alt="" loading="lazy" />}
                      <span className="fachada-hoja"></span>
                    </span>
                    <span className="fachada-nombre">{a.nombre}</span>
                  </Link>
                ))}
              </nav>
            )}
          </div>

          <form className="buscador" onSubmit={buscar} aria-label="Buscar fechas">
            <div className="buscador-campo">
              <label htmlFor="inicio-entrada">Llegada</label>
              <input id="inicio-entrada" type="date" value={entrada} min={dentroDe(0)}
                onChange={(e) => setEntrada(e.target.value)} required />
            </div>
            <div className="buscador-campo">
              <label htmlFor="inicio-salida">Salida</label>
              <input id="inicio-salida" type="date" value={salida} min={entrada}
                onChange={(e) => setSalida(e.target.value)} required />
            </div>
            <div className="buscador-campo buscador-personas">
              <label htmlFor="inicio-personas">Personas</label>
              <input id="inicio-personas" type="number" min={1} value={personas}
                onChange={(e) => setPersonas(Number(e.target.value))} required />
            </div>
            <button type="submit" className="btn btn-primary buscador-boton">Ver disponibilidad</button>
          </form>
        </div>
      </section>

      <section className="banda banda-oscura">
        <div className="container">
          <div className="row g-4 g-lg-5 align-items-center">
            <div className="col-12 col-lg-5">
              <h2 className="mb-4">Casas de bahareque, montaña y café a la vuelta de la esquina</h2>
              <p className="texto-suave mb-4">
                Camina a la plaza en cinco minutos, toma un Willys hacia el Cocora en la mañana y vuelve a cocinar
                en tu propio apartamento. La llegada es desde las 3:00 p. m. y la salida hasta las 11:00 a. m.
              </p>
              <Link to="/alojamiento" className="enlace-linea">Normas, horarios y servicios</Link>
            </div>
            <div className="col-12 col-lg-7">
              <img src={FOTO_SALENTO_CALLE} alt="Jeeps Willys en el parque principal de Salento" className="lugar-foto" loading="lazy" />
            </div>
          </div>
        </div>
      </section>

      <section className="banda banda-crema">
        <div className="container">
          <div className="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
            <h2 className="mb-0">Elige tu puerta</h2>
            <Link to="/disponibilidad" className="btn btn-outline-primary">Buscar por fechas</Link>
          </div>

          {cargando && <Cargando />}
          {!cargando && error && <div className="alert alert-danger">{error}</div>}
          {!cargando && !error && (
            <div className="mosaico">
              {activos.map((a) => (
                <Link key={a.identificacion} to={`/apartamentos/${a.identificacion}`} className="mosaico-item">
                  {a.imagenes[0] && <img src={miniatura(a.imagenes[0], 700)} alt="" loading="lazy" />}
                  <span className="mosaico-texto">
                    <Puerta identificacion={a.identificacion} tamano="md" />
                    <span>
                      <span className="mosaico-nombre">{a.nombre}</span>
                      <span className="mosaico-detalle">
                        {a.dormitorios === 1 ? '1 dormitorio' : `${a.dormitorios} dormitorios`}, hasta {a.capacidadMaxima} personas
                      </span>
                    </span>
                  </span>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>

      <section className="banda banda-negra">
        <div className="container">
          <p className="declaracion">
            Una puerta para cada grupo: de la pareja que viene por el café
            a la familia de <em>diez</em> que madruga para subir al Cocora.
          </p>
        </div>
      </section>

      <section className="banda banda-crema">
        <div className="container">
          <h2 className="mb-5">Cómo reservar</h2>
          <ol className="pasos">
            <li className="paso"><h3>Busca tus fechas</h3><p>Ves qué apartamentos están libres y cuánto cuesta cada noche.</p></li>
            <li className="paso"><h3>Elige tu puerta</h3><p>Compara capacidad, dormitorios y si admite mascotas.</p></li>
            <li className="paso"><h3>Registra al grupo</h3><p>El titular y quienes lo acompañan; los niños pequeños no pagan.</p></li>
            <li className="paso"><h3>Paga el anticipo</h3><p>En recepción, en efectivo o con tarjeta. Al registrarlo, la reserva queda confirmada.</p></li>
          </ol>
        </div>
      </section>

      <section className="cierre" style={{ backgroundImage: `url(${FOTO_SALENTO_MONTANA})` }}>
        <div className="container d-flex flex-wrap justify-content-between align-items-center gap-4">
          <h2 className="cierre-titulo mb-0">Tu puerta en Salento te espera</h2>
          <Link to="/disponibilidad" className="btn btn-crema btn-lg px-4">Reservar ahora</Link>
        </div>
      </section>
    </>
  );
}
