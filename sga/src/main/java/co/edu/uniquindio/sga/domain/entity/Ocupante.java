package co.edu.uniquindio.sga.domain.entity;

import java.time.LocalDate;

import java.time.Period;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import co.edu.uniquindio.sga.domain.valueobject.Estancia;

import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

public class Ocupante {

    private final DocumentoIdentidad documento;      

    private final LocalDate fechaNacimiento;        

    private String nombre;

    public Ocupante(DocumentoIdentidad documento, String nombre,

                    LocalDate fechaNacimiento, LocalDate fechaActual) {

        if (documento == null) {

            throw new ReglaDominioException("El ocupante debe tener documento de identidad");

        }

        if (nombre == null || nombre.isBlank()) {

            throw new ReglaDominioException("El ocupante debe tener nombre");

        }

        if (fechaNacimiento == null) {

            throw new ReglaDominioException("El ocupante debe tener fecha de nacimiento");

        }

        if (fechaNacimiento.isAfter(fechaActual)) {

            throw new ReglaDominioException("La fecha de nacimiento no puede ser futura");

        }

        this.documento = documento;

        this.nombre = nombre;

        this.fechaNacimiento = fechaNacimiento;

    }

    /** La edad se calcula. Nunca se almacena, porque cambiaría sola con el tiempo. */

    public int edadA(LocalDate fecha) {

        return Period.between(fechaNacimiento, fecha).getYears();

    }

    /**

     * RN-06: es facturable si a la FECHA DE ENTRADA alcanza el umbral.

     * Quien cumple años durante la estancia no cambia de condición a mitad de camino.

     */

    public boolean esFacturableEn(Estancia estancia, UmbralEdadFacturable umbral) {

        return edadA(estancia.fechaEntrada()) >= umbral.anios();

    }

    public void actualizarNombre(String nuevoNombre) {

        if (nuevoNombre == null || nuevoNombre.isBlank()) {

            throw new ReglaDominioException("El ocupante debe tener nombre");

        }

        this.nombre = nuevoNombre;

    }

    public DocumentoIdentidad getDocumento() {

        return documento;

    }

    public String getNombre() {

        return nombre;

    }

    public LocalDate getFechaNacimiento() {

        return fechaNacimiento;

    }

    @Override

    public boolean equals(Object o) {

        if (this == o) {

            return true;

        }

        if (!(o instanceof Ocupante otro)) {

            return false;

        }

        return this.documento.equals(otro.documento);

    }

    @Override

    public int hashCode() {

        return documento.hashCode();

    }

}

