package com.rica.publicaciones_service.publicaciones;

import org.springframework.stereotype.Service;

@Service
public class LimitePublicacionesAnualesService {

    private static final int MAXIMO_POR_ANIO = 5;

    private final PublicacionRepository publicacionRepository;

    public LimitePublicacionesAnualesService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    public boolean puedeRegistrar(String investigadorCorreo, int anio) {
        long registradasEsteAnio = publicacionRepository.countByInvestigadorCorreoAndAnio(
                investigadorCorreo, anio);
        return registradasEsteAnio < MAXIMO_POR_ANIO;
    }
}