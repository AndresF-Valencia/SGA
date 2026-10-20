package co.edu.uniquindio.sga.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.sga.domain.repository.CotizacionRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;

@Service
public class CotizarEstanciaUseCase {
    private final CotizacionRepository cotizacionRepository;
    private final TemporadaRepository temporadaRepository;
    private final CotizadorEstanciaService cotizadorEstanciaService;

    public CotizarEstanciaUseCase(CotizacionRepository cotizacionRepository,
            TemporadaRepository temporadaRepository, CotizadorEstanciaService cotizadorEstanciaService) {
        this.cotizacionRepository = cotizacionRepository;
        this.temporadaRepository = temporadaRepository;
        this.cotizadorEstanciaService = cotizadorEstanciaService;
    }

    @Transactional
    public void ejecutar() {

    }

}
