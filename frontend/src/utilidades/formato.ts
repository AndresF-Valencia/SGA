// Formato de dinero y fechas para mostrar. Los montos llegan del backend como pesos
// enteros (Long) y las fechas como "AAAA-MM-DD" (LocalDate). Lo usan tarjetas y páginas.

// Los formateadores se crean una vez y se reutilizan
const pesos = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

const fechaLarga = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
});

// 480000 → "$ 480.000"
export function formatearPesos(valor: number): string {
  return pesos.format(valor);
}

// "2026-11-20" → "20 nov 2026". La hora sin zona fuerza la hora local: no se corre un día
export function formatearFecha(fechaISO: string): string {
  return fechaLarga.format(new Date(`${fechaISO}T00:00:00`));
}
