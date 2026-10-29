package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import co.edu.uniquindio.sga.domain.repository.GeneradorCodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;

import java.util.concurrent.atomic.AtomicInteger;

/** Consecutivo en memoria: RES-2026-00001, RES-2026-00002, ... */
// Sin @Repository: la aplicación usa el adaptador JPA. Esta clase queda para las pruebas unitarias.
public class GeneradorCodigoReservaEnMemoria implements GeneradorCodigoReserva {

    private final AtomicInteger consecutivo = new AtomicInteger();

    @Override
    public CodigoReserva siguiente(int anio) {
        return new CodigoReserva(String.format("RES-%d-%05d", anio, consecutivo.incrementAndGet()));
    }
}