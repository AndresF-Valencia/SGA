// Pie de página común; Layout lo pinta debajo de todas las páginas.

// Se calcula una vez al cargar el módulo: el componente debe ser puro
const ANIO_ACTUAL = new Date().getFullYear();

// Muestra el año y el curso
export function PiePagina() {
  return (
    <footer className="bg-body-tertiary border-top py-3 mt-4">
      <div className="container text-center text-body-secondary small">
        © {ANIO_ACTUAL} Universidad del Quindío — Programación Avanzada
      </div>
    </footer>
  );
}
