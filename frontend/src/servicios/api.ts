// Cliente HTTP único de la app: todos los servicios (auth, reservas, cotización) lo usan.
// La URL apunta al Spring Boot del backend; el CORS de SecurityConfig ya autoriza localhost:5173.
import axios from 'axios';

// Instancia de Axios con la URL base de la API y un tiempo máximo de espera
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10_000,
});
