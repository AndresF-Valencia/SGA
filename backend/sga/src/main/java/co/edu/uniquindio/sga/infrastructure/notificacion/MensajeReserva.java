package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.domain.entity.Reserva;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Textos de los avisos al titular. Los usan los dos adaptadores (consola y correo),
 * así el mensaje es el mismo y solo cambia por dónde sale.
 */
public record MensajeReserva(String destinatario, String asunto, String cuerpo) {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"));

    public static MensajeReserva creada(Reserva reserva) {
        return new MensajeReserva(destino(reserva),
                "Recibimos tu reserva " + codigo(reserva) + " - San Rocco",
                saludo(reserva)
                        + "Recibimos tu reserva y quedó PENDIENTE de confirmación.\n\n"
                        + resumen(reserva)
                        + "\nPara confirmarla debes pagar el anticipo. Si no se confirma dentro del plazo, "
                        + "la reserva se cancela automáticamente y las noches quedan libres.\n"
                        + despedida());
    }

    public static MensajeReserva confirmada(Reserva reserva) {
        return new MensajeReserva(destino(reserva),
                "Tu reserva " + codigo(reserva) + " está confirmada - San Rocco",
                saludo(reserva)
                        + "¡Tu reserva está CONFIRMADA! Te esperamos.\n\n"
                        + resumen(reserva)
                        + despedida());
    }

    public static MensajeReserva cancelada(Reserva reserva) {
        return new MensajeReserva(destino(reserva),
                "Tu reserva " + codigo(reserva) + " fue cancelada - San Rocco",
                saludo(reserva)
                        + "Tu reserva fue CANCELADA. El valor que se retiene, si aplica, se calculó con la "
                        + "política de cancelación vigente cuando hiciste la reserva.\n\n"
                        + resumen(reserva)
                        + despedida());
    }

    // ---------- Partes comunes ----------

    private static String destino(Reserva reserva) {
        return reserva.getCorreoContacto() == null ? null : reserva.getCorreoContacto().valor();
    }

    private static String codigo(Reserva reserva) {
        return reserva.getCodigo().valor();
    }

    private static String saludo(Reserva reserva) {
        return "Hola, " + reserva.getTitular().getNombre() + ":\n\n";
    }

    private static String resumen(Reserva reserva) {
        NumberFormat pesos = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        pesos.setMaximumFractionDigits(0);
        return "  Código:       " + codigo(reserva) + "\n"
                + "  Apartamento:  " + reserva.getApartamento().valor() + "\n"
                + "  Entrada:      " + FECHA.format(reserva.getEstancia().fechaEntrada()) + "\n"
                + "  Salida:       " + FECHA.format(reserva.getEstancia().fechaSalida()) + "\n"
                + "  Noches:       " + reserva.getEstancia().noches() + "\n"
                + "  Ocupantes:    " + reserva.totalOcupantes() + "\n"
                + "  Valor total:  " + pesos.format(reserva.getValor().total().valor()) + "\n";
    }

    private static String despedida() {
        return "\nApartamentos San Rocco · Armenia, Quindío\n"
                + "Este es un mensaje automático, por favor no lo respondas.";
    }
}
