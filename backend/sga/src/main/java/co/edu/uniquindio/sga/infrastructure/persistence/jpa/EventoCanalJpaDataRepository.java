package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.EventoCanalEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoCanalJpaDataRepository extends JpaRepository<EventoCanalEntity, String> {

    // SELECT * FROM evento_canal ORDER BY ocurrido_en DESC LIMIT 500
    // Con tope: los canales son una entrada pública y la bitácora puede crecer mucho.
    List<EventoCanalEntity> findTop500ByOrderByOcurridoEnDesc();
}
