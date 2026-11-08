// Utilidades de fechas en hora LOCAL. Las usan los esquemas de Zod para comparar con
// "hoy" igual que el backend (LocalDate.now()), sin el corrimiento de toISOString() (UTC).

// Fecha de hoy como "AAAA-MM-DD" según el reloj del navegador
export function hoyISO(): string {
  const hoy = new Date();
  const mes = String(hoy.getMonth() + 1).padStart(2, '0');
  const dia = String(hoy.getDate()).padStart(2, '0');
  return `${hoy.getFullYear()}-${mes}-${dia}`;
}
