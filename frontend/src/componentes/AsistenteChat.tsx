// Widget flotante del chatbot FAQ. Solo se muestra a usuarios con sesión iniciada:
// el endpoint /api/chat exige JWT y el backend lo protege con SecurityConfig.
import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { sendChat, type ChatMessage } from '../servicios/chatServicio';
import './AsistenteChat.css';

const MAX_LENGTH = 500;

export function AsistenteChat() {
  const { autenticado } = useAuth();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const endRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  // Sin sesión no se pinta el botón ni el panel
  if (!autenticado) {
    return null;
  }

  async function send() {
    const text = input.trim();
    if (!text || loading) return;

    const next: ChatMessage[] = [...messages, { role: 'user', content: text }];
    setMessages(next);
    setInput('');
    setLoading(true);

    const reply = await sendChat(next);
    setMessages([...next, { role: 'assistant', content: reply }]);
    setLoading(false);
  }

  if (!open) {
    return (
      <button className="sga-chat-toggle" onClick={() => setOpen(true)} aria-label="Abrir asistente">
        Preguntas
      </button>
    );
  }

  return (
    <section className="sga-chat" aria-label="Asistente virtual">
      <header className="sga-chat__header">
        <span>Asistente virtual</span>
        <button onClick={() => setOpen(false)} aria-label="Cerrar asistente">
          Cerrar
        </button>
      </header>

      <div className="sga-chat__messages" role="log" aria-live="polite">
        {messages.length === 0 && (
          <p className="sga-chat__empty">
            Pregunta sobre el registro, tu cuenta o los recordatorios por correo.
          </p>
        )}
        {messages.map((m, i) => (
          <p key={i} className={`sga-chat__msg sga-chat__msg--${m.role}`}>
            {m.content}
          </p>
        ))}
        {loading && <p className="sga-chat__msg sga-chat__msg--assistant">Escribiendo...</p>}
        <div ref={endRef} />
      </div>

      <div className="sga-chat__input">
        <input
          value={input}
          maxLength={MAX_LENGTH}
          disabled={loading}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && send()}
          placeholder="Escribe tu pregunta"
          aria-label="Tu pregunta"
        />
        <button onClick={send} disabled={loading || !input.trim()}>
          Enviar
        </button>
      </div>
    </section>
  );
}
