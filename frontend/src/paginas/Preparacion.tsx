// Página provisional "/preparacion" (PERSONAL_SERVICIO, ADMINISTRADOR). Consumirá
// POST /api/apartamentos/{id}/estado-operativo.

// Solo el título por ahora
export default function Preparacion() {
  return (
    <section>
      <h1 className="h3">Preparación de apartamentos</h1>
      <p className="text-body-secondary">
        Apartamentos pendientes de preparación y cambio de su estado operativo (7.1).
      </p>
    </section>
  );
}
