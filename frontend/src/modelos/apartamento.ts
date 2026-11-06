// Vista breve de un apartamento. Usa los mismos nombres de campo que
// ApartamentoDetalleResponse.java para que, al conectar GET /api/apartamentos,
// los datos del backend encajen sin traducir. Lo usan las tarjetas y el detalle.
export interface ApartamentoResumen {
  identificacion: string; // "AP001": identidad del negocio, igual que en el backend
  nombre: string;
  dormitorios: number;
  capacidadMaxima: number; // tope rígido de personas
  limiteMascotas: number;
  caracteristicas: string[];
}
