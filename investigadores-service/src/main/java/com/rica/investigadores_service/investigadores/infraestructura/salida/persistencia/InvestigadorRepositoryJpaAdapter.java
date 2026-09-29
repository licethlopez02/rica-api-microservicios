package com.rica.investigadores_service.investigadores.infraestructura.salida.persistencia;


import org.springframework.stereotype.Component;

import com.rica.investigadores_service.investigadores.dominio.Investigador;
import com.rica.investigadores_service.investigadores.aplicacion.RepositorioInvestigadores;

import java.util.List;
import java.util.Optional;

@Component
public class InvestigadorRepositoryJpaAdapter implements RepositorioInvestigadores{
    
    private final InvestigadorRepository investigadorRepository;

    public InvestigadorRepositoryJpaAdapter(InvestigadorRepository investigadorRepository) {
        this.investigadorRepository = investigadorRepository;
    }

    public List<Investigador> listarTodos() {
        return investigadorRepository.findAll();
    }

    public Optional<Investigador> buscarPorId(Long id) {
        return investigadorRepository.findById(id);
    }

    public boolean existeCorreo(String correoInstitucional) {
        return investigadorRepository.existsByCorreoInstitucional_Valor(correoInstitucional);
    }

    public Investigador guardar(Investigador investigador) {
        return investigadorRepository.save(investigador);
    }
}
