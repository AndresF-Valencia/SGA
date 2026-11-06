// Punto de entrada: monta <App /> en el #root de index.html dentro del enrutador,
// para que todas las páginas puedan usar Link, useParams y useNavigate.
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router';
import App from './App.tsx';
import './index.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
);
