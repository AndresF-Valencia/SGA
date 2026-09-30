package co.edu.uniquindio.sga.domain.entity;

import java.time.LocalDate;

import java.util.List;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;

import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

public class Reserva {

    private final CodigoReserva codigo;          

    private final CanalOrigen canalOrigen;       

    private final LocalDate fechaCreacion;

    private final Ocupante titular;

    private Apartamento apartamento;             

    private Estancia estancia;

    private List<Ocupante> ocupantes;

    private EstadoReserva estado;
    

    private Reserva(CodigoReserva codigo, Apartamento apartamento, Estancia estancia,

                    Ocupante titular, List<Ocupante> ocupantes,

                    CanalOrigen canalOrigen, LocalDate fechaCreacion) {

        this.codigo = codigo;

        this.apartamento = apartamento;

        this.estancia = estancia;

        this.titular = titular;

        this.ocupantes = List.copyOf(ocupantes);      // copia inmutable: nadie la altera por fuera

        this.canalOrigen = canalOrigen;

        this.fechaCreacion = fechaCreacion;

        this.estado = EstadoReserva.PENDIENTE;        // toda reserva nace PENDIENTE

    }

    /**

     * Única puerta de entrada para crear una reserva.

     * El nombre viene del lenguaje del dominio, no de la técnica.

     */

    public static Reserva crear(CodigoReserva codigo, Apartamento apartamento,

                                Estancia estancia, Ocupante titular,

                                List<Ocupante> ocupantes, CanalOrigen canalOrigen,

                                LocalDate fechaActual) {

        if (codigo == null) {

            throw new ReglaDominioException("La reserva debe tener un código");

        }

        if (apartamento == null) {

            throw new ReglaDominioException("La reserva debe tener un apartamento");

        }

        if (estancia == null) {

            throw new ReglaDominioException("La reserva debe tener una estancia");

        }

        if (titular == null) {

            throw new ReglaDominioException("La reserva debe tener un titular");

        }

        if (canalOrigen == null) {

            throw new ReglaDominioException("La reserva debe indicar su canal de origen");

        }

        if (ocupantes == null || ocupantes.isEmpty()) {

            throw new ReglaDominioException("La reserva debe tener al menos un ocupante");

        }

        if (!ocupantes.contains(titular)) {

            throw new ReglaDominioException("El titular debe ser uno de los ocupantes");

        }

        if (ocupantes.stream().distinct().count() != ocupantes.size()) {

            throw new ReglaDominioException("No se puede repetir un ocupante en la reserva");

        }

        // RN-04: no se crean reservas hacia el pasado

        if (estancia.fechaEntrada().isBefore(fechaActual)) {

            throw new ReglaDominioException(

                "La fecha de entrada no puede ser anterior a la fecha actual");

        }

        if (!apartamento.estaActivo()) {

            throw new ReglaDominioException("El apartamento no está disponible para la venta");

        }

        // RN-02: la capacidad es un tope rígido

        if (!apartamento.admite(ocupantes.size())) {

            throw new ReglaDominioException(

                "El número de ocupantes excede la capacidad del apartamento");

        }

        return new Reserva(codigo, apartamento, estancia, titular,

                           ocupantes, canalOrigen, fechaActual);

    }

    /** Cuántas personas ocupan el apartamento. Todas cuentan para la capacidad. */

    public int totalOcupantes() {

        return ocupantes.size();

    }

    public CodigoReserva getCodigo() {

        return codigo;

    }

    public EstadoReserva getEstado() {

        return estado;

    }

    public Estancia getEstancia() {

        return estancia;

    }

    public Apartamento getApartamento() {

        return apartamento;

    }

    public Ocupante getTitular() {

        return titular;

    }

    /** Lista inmutable: agregar ocupantes exige pasar por el comportamiento del dominio. */

    public List<Ocupante> getOcupantes() {

        return ocupantes;

    }

    public CanalOrigen getCanalOrigen() {

        return canalOrigen;

    }

    public LocalDate getFechaCreacion() {

        return fechaCreacion;

    }

    @Override

    public boolean equals(Object o) {

        if (this == o) {

            return true;

        }

        if (!(o instanceof Reserva otra)) {

            return false;

        }

        return this.codigo.equals(otra.codigo);

    }

    @Override

    public int hashCode() {

        return codigo.hashCode();

    }

}
