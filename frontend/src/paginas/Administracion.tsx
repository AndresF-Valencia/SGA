// Página provisional "/administracion" (ADMINISTRADOR). Consumirá usuarios, apartamentos,
// temporadas, tarifas, bloqueos, políticas y parámetros del alojamiento.

// Solo el título por ahora
export default function Administracion() {
  return (
    <section>
      <h1 className="h3">Administración del alojamiento</h1>
      <p className="text-body-secondary">
        Apartamentos, temporadas, tarifas y parámetros (7.1). Solo para el rol ADMINISTRADOR.
      </p>
    </section>
  );
}
