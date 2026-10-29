package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ContadorReservaEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genera RES-2026-00001, RES-2026-00002... guardando el último número en la base.
 * Así, si la base guarda los datos entre reinicios, nunca se repite un código.
 */
@Repository
public class GeneradorCodigoReservaJpa implements GeneradorCodigoReserva {

    private final ContadorReservaJpaDataRepository jpa;

    public GeneradorCodigoReservaJpa(ContadorReservaJpaDataRepository jpa) {
        this.jpa = jpa;
    }

    /** @Transactional: leer, sumar 1 y guardar pasa como una sola operación. */
    @Override
    @Transactional
    public CodigoReserva siguiente(int anio) {
        ContadorReservaEntity contador = jpa.findByAnio(anio).orElseGet(() -> {
            ContadorReservaEntity nuevo = new ContadorReservaEntity();
            nuevo.setAnio(anio);
            nuevo.setUltimo(0);
            return nuevo;
        });
        contador.setUltimo(contador.getUltimo() + 1);
        jpa.save(contador);
        return new CodigoReserva(String.format("RES-%d-%05d", anio, contador.getUltimo()));
    }
}
