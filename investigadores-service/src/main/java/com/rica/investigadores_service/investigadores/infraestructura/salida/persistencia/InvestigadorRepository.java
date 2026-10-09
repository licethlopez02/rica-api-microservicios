package com.rica.investigadores_service.investigadores.infraestructura.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rica.investigadores_service.investigadores.dominio.Investigador;

import java.util.Optional;

public interface InvestigadorRepository extends JpaRepository<Investigador, Long> {

    boolean existsByCorreoInstitucional_Valor(String valor);

    Optional<Investigador> findByCorreoInstitucional_Valor(String correoInstitucional);
}
