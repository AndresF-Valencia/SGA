package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;

import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

public class Apartamento {

    private final IdentificacionApartamento identificacion;  

    private String nombre;                                   

    private int dormitorios;

    private int capacidad;

    private EstadoOperativo estadoOperativo;

    private boolean activo;

    
    public Apartamento(IdentificacionApartamento identificacion, String nombre,

                       int dormitorios, int capacidad) {

        if (identificacion == null) {

            throw new ReglaDominioException("El apartamento debe tener identificación");

        }

        if (nombre == null || nombre.isBlank()) {

            throw new ReglaDominioException("El apartamento debe tener un nombre");

        }

        if (dormitorios < 1) {

            throw new ReglaDominioException("El apartamento debe tener al menos un dormitorio");

        }

        if (capacidad < 1) {

            throw new ReglaDominioException("La capacidad del apartamento debe ser al menos 1");

        }

        this.identificacion = identificacion;

        this.nombre = nombre;

        this.dormitorios = dormitorios;

        this.capacidad = capacidad;

        this.estadoOperativo = EstadoOperativo.PREPARADO;

        this.activo = true;

    }

    // Comportamiento: los nombres vienen del lenguaje del dominio

    /** RN-02: la capacidad es un tope rígido, sin excepciones. */

    public boolean admite(int totalOcupantes) {

        return totalOcupantes > 0 && totalOcupantes <= capacidad;

    }

    /** RN-11: solo se entrega un apartamento activo y PREPARADO. */

    public boolean puedeRecibirGrupo() {

        return activo && estadoOperativo.permiteRegistro();

    }

    /** La eliminación de apartamentos es lógica (7.3). */

    public void desactivar() {

        this.activo = false;

    }

    public IdentificacionApartamento getIdentificacion() {

        return identificacion;

    }

    public String getNombre() {

        return nombre;

    }

    public int getDormitorios() {

        return dormitorios;

    }

    public int getCapacidad() {

        return capacidad;

    }

    public EstadoOperativo getEstadoOperativo() {

        return estadoOperativo;

    }

    public boolean estaActivo() {

        return activo;

    }

    // Dos apartamentos son el mismo si tienen la misma identificación,

    // aunque hayan cambiado de nombre, de dotación o de estado.

    @Override

    public boolean equals(Object o) {

        if (this == o) {

            return true;

        }

        if (!(o instanceof Apartamento otro)) {

            return false;

        }

        return this.identificacion.equals(otro.identificacion);

    }

    @Override

    public int hashCode() {

        return identificacion.hashCode();

    }

}
