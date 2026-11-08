import { useEffect, useState } from 'react';

export function useDebounce<T>(valor: T, retardo = 400): T {
  const [valorEstable, setValorEstable] = useState(valor);

  useEffect(() => {
    const temporizador = setTimeout(() => setValorEstable(valor), retardo);
    return () => clearTimeout(temporizador);
  }, [valor, retardo]);

  return valorEstable;
}