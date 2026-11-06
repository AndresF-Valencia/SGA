// Pie de página común; App.tsx lo pinta debajo de todas las rutas.

// Se calcula una vez al cargar el módulo: el componente debe ser puro
const ANIO_ACTUAL = new Date().getFullYear();

// Muestra el año y el curso
export function PiePagina() {
  return <footer>© {ANIO_ACTUAL} Universidad del Quindío — Programación Avanzada</footer>;
}
