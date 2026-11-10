// Llamada al chatbot FAQ (POST /api/chat). Usa la instancia Axios de la app para que el
// interceptor agregue el JWT: el endpoint solo responde a usuarios con sesión iniciada.
import axios from 'axios';
import { api } from './api';

export type ChatRole = 'user' | 'assistant';

export interface ChatMessage {
  role: ChatRole;
  content: string;
}

// Devuelve siempre un texto para mostrar en el chat, incluso cuando hay error.
export async function sendChat(messages: ChatMessage[]): Promise<string> {
  try {
    // El chatbot puede tardar más que una consulta normal; se amplía el timeout de 10 s.
    const { data } = await api.post<{ reply: string }>('/chat', { messages }, { timeout: 30_000 });
    return data.reply;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;

      if (status === 429) {
        return 'Demasiadas preguntas, espera un momento.';
      }

      // 400 y 503 traen { reply } con un mensaje amable del backend
      const reply = (error.response?.data as { reply?: string } | undefined)?.reply;
      if (reply) {
        return reply;
      }

      if (status === 401) {
        return 'Tu sesión expiró. Vuelve a iniciar sesión para usar el asistente.';
      }
    }

    return 'No pudimos conectar con el asistente. Intenta de nuevo en unos minutos.';
  }
}
