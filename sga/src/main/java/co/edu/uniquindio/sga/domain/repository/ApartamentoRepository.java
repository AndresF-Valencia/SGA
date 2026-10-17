package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Apartamento;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionApartamento;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del agregado Apartamento. */
public interface ApartamentoRepository {

    void guardar(Apartamento apartamento);

    Optional<Apartamento> buscarPorId(IdentificacionApartamento id);

    /** Solo los apartamentos activos: la eliminación es lógica (7.3). */
    List<Apartamento> listarActivos();

    List<Apartamento> listar();
}
