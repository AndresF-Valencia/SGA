package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.valueobject.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.util.Optional;

/**
 * Puerto de salida de las políticas de cancelación (F-07, 7.4).
 * Guarda cada versión publicada, para que una reserva siempre se evalúe con la
 * política que quedó congelada al crearla (RN-13, RN-22), aunque hoy rija otra.
 */
public interface PoliticaCancelacionRepository {

    /**
     * Guarda una versión de la política. Las versiones anteriores se conservan,
     * nunca se borran.
     */
    void guardar(VersionPolitica version, PoliticaCancelacion politica);

    /**
     * Busca la política de una versión, por ejemplo la que quedó congelada en una
     * reserva.
     */
    Optional<PoliticaCancelacion> buscarPorVersion(VersionPolitica version);
}