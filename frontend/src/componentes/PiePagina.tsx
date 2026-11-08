// Pie de página común; Layout lo pinta debajo de todas las páginas.

// Se calcula una vez al cargar el módulo: el componente debe ser puro
const ANIO_ACTUAL = new Date().getFullYear();

// Muestra el año y el curso
export function PiePagina() {
  return (
    <footer className="pie py-4">
      <div className="container d-flex flex-column flex-md-row justify-content-between gap-2 small">
        <span>Apartamentos Turísticos San Rocco, Salento (Quindío)</span>
        <span>Programación Avanzada, Universidad del Quindío, {ANIO_ACTUAL}. Fotos ilustrativas de <a href="https://unsplash.com" target="_blank" rel="noreferrer">Unsplash</a>.</span>
      </div>
    </footer>
  );
}
