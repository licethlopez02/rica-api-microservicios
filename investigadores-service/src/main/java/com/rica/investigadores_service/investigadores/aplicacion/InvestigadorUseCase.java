package com.rica.investigadores_service.investigadores.aplicacion;


import java.util.List;

import com.rica.investigadores_service.investigadores.dominio.Investigador;

public interface InvestigadorUseCase {
    
    List<Investigador> listarTodos();
    Investigador buscarPorId(Long id);
    Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion);

}
