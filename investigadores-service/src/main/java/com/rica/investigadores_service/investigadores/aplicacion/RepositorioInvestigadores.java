package com.rica.investigadores_service.investigadores.aplicacion;

import java.util.List;
import java.util.Optional;

import com.rica.investigadores_service.investigadores.dominio.Investigador;

public interface RepositorioInvestigadores {

    List<Investigador> listarTodos();
    Optional<Investigador> buscarPorId(Long id);
    boolean existeCorreo(String correoInstitucional);
    Investigador guardar(Investigador investigador);
    Optional<Investigador> buscarPorCorreo(String correoInstitucional);
    void eliminar(Investigador investigador);
    
}
