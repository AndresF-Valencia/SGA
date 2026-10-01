package co.edu.uniquindio.sga.domain.valueobject;

import java.math.BigDecimal;

import java.math.RoundingMode;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**

 * Valor monetario en pesos colombianos, sin decimales (definición 3.4).

 * Se admiten valores negativos: los ajustes por modificación y los saldos

 * a favor del huésped lo son.

 */

public record Dinero(BigDecimal valor) {

    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);

    public Dinero {

        if (valor == null) {

            throw new ReglaDominioException("El valor monetario es obligatorio");

        }

        valor = valor.setScale(0, RoundingMode.HALF_UP);

    }

    public static Dinero de(long pesos) {

        return new Dinero(BigDecimal.valueOf(pesos));

    }

    public Dinero mas(Dinero otro) {

        return new Dinero(this.valor.add(otro.valor));

    }

    public Dinero menos(Dinero otro) {

        return new Dinero(this.valor.subtract(otro.valor));

    }

    public Dinero por(int cantidad) {

        return new Dinero(this.valor.multiply(BigDecimal.valueOf(cantidad)));

    }

    public boolean esCero() {

        return this.valor.signum() == 0;

    }

    public boolean esNegativo() {

        return this.valor.signum() < 0;

    }

}

