package com.rica.publicaciones_service.publicaciones;

import com.rica.publicaciones_service.compartido.LimiteAnualExcedidoException;
import com.rica.publicaciones_service.compartido.RecursoNoEncontradoException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final VerificadorInvestigador verificadorInvestigador;
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    public PublicacionService(PublicacionRepository publicacionRepository,
                              VerificadorInvestigador verificadorInvestigador,
                              LimitePublicacionesAnualesService limitePublicacionesAnualesService)
    {
        this.publicacionRepository = publicacionRepository;
        this.verificadorInvestigador = verificadorInvestigador;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
    }

    public Publicacion registrar(Publicacion publicacion) {
        if (!verificadorInvestigador.existe(publicacion.getInvestigadorCorreo())) {
            throw new RecursoNoEncontradoException("No existe un investigador con correo " + publicacion.getInvestigadorCorreo());
        }

        if (!limitePublicacionesAnualesService.puedeRegistrar(
            publicacion.getInvestigadorCorreo(), publicacion)) {
            throw new LimiteAnualExcedidoException(
                    "El investigador ha superado el límite máximo de publicaciones para el año " + publicacion.getAnio());
        }

        return publicacionRepository.save(publicacion);
    }

    public List<Publicacion> listarPorInvestigador(String investigadorCorreo) {
        return publicacionRepository.findByInvestigadorCorreo(investigadorCorreo);
    }

    public Publicacion buscarPorId(String id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una publicación con id " + id));
    }
}
