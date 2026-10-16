package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.valueobject.Disponibilidad;
import co.edu.uniquindio.sga.domain.valueobject.IndentificacionCotizacion;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.valueobject.CantidadMascota;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;

public class Cotizacion {
    private final IndentificacionCotizacion identificacionCotizacion;
    private final IdentificacionApartamento identificacionApartamento;
    private final Dinero montoFinal;

    private Estancia estancia;
    private Disponibilidad disponibilidad;
    private CantidadMascota mascota;

    public Cotizacion(IndentificacionCotizacion identificacionCotizacion,
            IdentificacionApartamento identificacionApartamento, Dinero montoFinal, Estancia estancia,
            Disponibilidad disponibilidad, CantidadMascota mascota) {
        this.identificacionCotizacion = identificacionCotizacion;
        this.identificacionApartamento = identificacionApartamento;
        this.montoFinal = montoFinal;
        this.estancia = estancia;
        this.disponibilidad = disponibilidad;
        this.mascota = mascota;
    }

    /**
     * apartado 7.4: Calcular el valor de una estancia antes de reservar.
     */

    public void calcularEstancia() {

    }

    /**
     * apartado 7.4: Mostrar el desglose noche por noche
     */
    public void mostrarDesglozeNoche() {

    }

    // ----- CONSULTAS -----

    public Estancia getEstancia() {
        return estancia;
    }

    public IndentificacionCotizacion getIdentificacionCotizacion() {
        return identificacionCotizacion;
    }

    public IdentificacionApartamento getIdentificacionApartamento() {
        return identificacionApartamento;
    }

    public Dinero getMontoFinal() {
        return montoFinal;
    }

    public void setEstancia(Estancia estancia) {
        this.estancia = estancia;
    }

    public Disponibilidad getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(Disponibilidad disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public CantidadMascota getMascota() {
        return mascota;
    }

    public void setMascota(CantidadMascota mascota) {
        this.mascota = mascota;
    }
}