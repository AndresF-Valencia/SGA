package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.EventoCanal;

import java.util.List;

/** Puerto de salida de la bitácora de canales (7.8). Solo se agrega y se consulta. */
public interface EventoCanalRepository {

    void guardar(EventoCanal evento);

    /** Los 500 más recientes, del más reciente al más antiguo. */
    List<EventoCanal> listar();
}
