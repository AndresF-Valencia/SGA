import Swal from 'sweetalert2';

const toast = Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
});

export function notificarExito(mensaje: string) {
  return toast.fire({ icon: 'success', title: mensaje });
}

export function notificarError(mensaje: string) {
  return toast.fire({ icon: 'error', title: mensaje });
}

export async function confirmarAccion(titulo: string, texto: string): Promise<boolean> {
  const resultado = await Swal.fire({
    icon: 'question',
    title: titulo,
    text: texto,
    showCancelButton: true,
    confirmButtonText: 'Confirmar',
    cancelButtonText: 'Cancelar',
  });
  return resultado.isConfirmed;
}

export async function pedirMotivo(titulo: string): Promise<string | null> {
  const resultado = await Swal.fire({
    input: 'textarea',
    inputLabel: 'Motivo',
    inputPlaceholder: 'Escriba el motivo de la cancelación',
    inputAttributes: { 'aria-label': 'Motivo de la cancelación' },
    title: titulo,
    showCancelButton: true,
    confirmButtonText: 'Cancelar reserva',
    cancelButtonText: 'Volver',
    inputValidator: (valor) => {
      if (!valor || valor.trim().length < 5) return 'El motivo debe tener al menos 5 caracteres.';
      if (valor.trim().length > 300) return 'El motivo no puede superar 300 caracteres.';
      return undefined;
    },
  });
  return resultado.isConfirmed && typeof resultado.value === 'string' ? resultado.value.trim() : null;
}