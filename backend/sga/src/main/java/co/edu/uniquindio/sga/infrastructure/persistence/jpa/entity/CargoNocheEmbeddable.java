package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Embeddable
public class CargoNocheEmbeddable {

    @Column(name = "noche", nullable = false)
    private LocalDate noche;

    @Column(name = "temporada", nullable = false, length = 30)
    private String temporada;

    @Column(name = "tarifa_por_ocupante", nullable = false, precision = 15, scale = 2)
    private BigDecimal tarifaPorOcupante; // en el dominio es Dinero

    @Column(name = "ocupantes_facturables", nullable = false)
    private int ocupantesFacturables;

    protected CargoNocheEmbeddable() {
    }

    public CargoNocheEmbeddable(LocalDate noche, String temporada,
            BigDecimal tarifaPorOcupante, int ocupantesFacturables) {
        this.noche = noche;
        this.temporada = temporada;
        this.tarifaPorOcupante = tarifaPorOcupante;
        this.ocupantesFacturables = ocupantesFacturables;
    }

    public LocalDate getNoche() {
        return noche;
    }

    public String getTemporada() {
        return temporada;
    }

    public BigDecimal getTarifaPorOcupante() {
        return tarifaPorOcupante;
    }

    public int getOcupantesFacturables() {
        return ocupantesFacturables;
    }
}