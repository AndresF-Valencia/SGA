package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.CredencialCanalInvalidaException;
import co.edu.uniquindio.sga.domain.entity.Canal;
import co.edu.uniquindio.sga.domain.repository.CanalRepository;

/**
 * 10.3.1: las operaciones del canal externo se autentican con una credencial por canal.
 * Devuelve el canal dueño de la credencial; si no existe o está inactivo, lo rechaza.
 * Así cada reserva externa queda ligada al canal que de verdad la envió.
 */
public class AutenticarCanalUseCase {

    private final CanalRepository canalRepository;

    public AutenticarCanalUseCase(CanalRepository canalRepository) {
        this.canalRepository = canalRepository;
    }

    public Canal ejecutar(String credencial) {
        if (credencial == null || credencial.isBlank()) {
            throw new CredencialCanalInvalidaException();
        }
        return canalRepository.buscarPorCredencial(credencial)
                .filter(Canal::isActivo)
                .orElseThrow(CredencialCanalInvalidaException::new);
    }
}
