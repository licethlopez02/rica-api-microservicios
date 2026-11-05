package com.rica.investigadores_service.investigadores.aplicacion;

import com.rica.investigadores_service.compartido.RecursoNoEncontradoException;
import com.rica.investigadores_service.investigadores.dominio.CorreoInstitucional;
import com.rica.investigadores_service.investigadores.dominio.Investigador;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InvestigadorServiceTest {

    private final RepositorioInvestigadores repositorioInvestigadores = mock(RepositorioInvestigadores.class);
    private final InvestigadorFactory investigadorFactory = mock(InvestigadorFactory.class);
    private final PublicadorEventos publicadorEventos = mock(PublicadorEventos.class);
    private final InvestigadorService investigadorService = new InvestigadorService(
            repositorioInvestigadores,
            investigadorFactory,
            publicadorEventos);

    @Test
    void listarTodosDevuelveLosInvestigadoresDelRepositorio() {
        List<Investigador> investigadores = List.of(crearInvestigador());
        when(repositorioInvestigadores.listarTodos()).thenReturn(investigadores);

        List<Investigador> resultado = investigadorService.listarTodos();

        assertSame(investigadores, resultado);
        verify(repositorioInvestigadores).listarTodos();
    }

    @Test
    void buscarPorIdDevuelveElInvestigadorCuandoExiste() {
        Investigador investigador = crearInvestigador();
        when(repositorioInvestigadores.buscarPorId(1L)).thenReturn(Optional.of(investigador));

        Investigador resultado = investigadorService.buscarPorId(1L);

        assertSame(investigador, resultado);
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repositorioInvestigadores.buscarPorId(42L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException excepcion = assertThrows(
                RecursoNoEncontradoException.class,
                () -> investigadorService.buscarPorId(42L));

        assertEquals("No existe un investigador con id 42", excepcion.getMessage());
    }

    @Test
    void registrarGuardaAlInvestigadorYPublicaElEventoDeRegistro() {
        String correo = "ana.torres@uptc.edu.co";
        Investigador creado = crearInvestigador();
        Investigador guardado = new Investigador(
                7L, "Ana Torres", new CorreoInstitucional(correo), "GIT-UPTC");
        when(investigadorFactory.crear("Ana Torres", correo, "GIT-UPTC")).thenReturn(creado);
        when(repositorioInvestigadores.guardar(creado)).thenReturn(guardado);

        Investigador resultado = investigadorService.registrar("Ana Torres", correo, "GIT-UPTC");

        assertSame(guardado, resultado);
        verify(investigadorFactory).crear("Ana Torres", correo, "GIT-UPTC");
        verify(repositorioInvestigadores).guardar(creado);
        verify(publicadorEventos).publicar(
                "investigadores.registrados",
                correo,
                Map.of("id", 7L, "correoInstitucional", correo, "nombreCompleto", "Ana Torres"));
    }

    @Test
    void iniciarEliminacionMarcaGuardaYPublicaElEvento() {
        String correo = "ana.torres@uptc.edu.co";
        Investigador investigador = crearInvestigador();
        when(repositorioInvestigadores.buscarPorId(1L)).thenReturn(Optional.of(investigador));

        investigadorService.iniciarEliminacion(1L);

        assertTrue(investigador.estaEnEliminacion());
        verify(repositorioInvestigadores).guardar(investigador);
        verify(publicadorEventos).publicar(
                "investigadores.en-eliminacion",
                correo,
                Map.of("correoInstitucional", correo));
    }

    @Test
    void iniciarEliminacionNoGuardaNiPublicaSiYaEstaEnEliminacion() {
        Investigador investigador = crearInvestigador();
        investigador.marcarEnEliminacion();
        when(repositorioInvestigadores.buscarPorId(1L)).thenReturn(Optional.of(investigador));

        investigadorService.iniciarEliminacion(1L);

        verify(repositorioInvestigadores, never()).guardar(investigador);
        verifyNoInteractions(publicadorEventos);
    }

    @Test
    void iniciarEliminacionLanzaExcepcionSiElInvestigadorNoExiste() {
        when(repositorioInvestigadores.buscarPorId(42L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> investigadorService.iniciarEliminacion(42L));

        verify(repositorioInvestigadores, never()).guardar(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(publicadorEventos);
    }

    @Test
    void confirmarEliminacionNoBorraNiPublicaSiElInvestigadorNoEstaEnEliminacion() {
        String correo = "ana.torres@uptc.edu.co";
        Investigador investigador = crearInvestigador();
        when(repositorioInvestigadores.buscarPorCorreo(correo)).thenReturn(Optional.of(investigador));

        investigadorService.confirmarEliminacion(correo);

        verify(repositorioInvestigadores, never()).eliminar(investigador);
        verifyNoInteractions(publicadorEventos);
    }

    @Test
    void confirmarEliminacionNoHaceNadaSiElInvestigadorNoExiste() {
        when(repositorioInvestigadores.buscarPorCorreo("ana.torres@uptc.edu.co")).thenReturn(Optional.empty());

        investigadorService.confirmarEliminacion("ana.torres@uptc.edu.co");

        verify(repositorioInvestigadores, never()).eliminar(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(publicadorEventos);
    }

    private Investigador crearInvestigador() {
        return new Investigador(
                1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
    }

    @Test
    void confirmarEliminacionBorraAlInvestigadorYPublicaElEventoEliminado() {
        Investigador enEliminacion = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        enEliminacion.marcarEnEliminacion();
        when(repositorioInvestigadores.buscarPorCorreo("ana.torres@uptc.edu.co")).thenReturn(Optional.of(enEliminacion));

        investigadorService.confirmarEliminacion("ana.torres@uptc.edu.co");

        verify(repositorioInvestigadores).eliminar(enEliminacion);
        verify(publicadorEventos).publicar(
                "investigadores.eliminados",
                "ana.torres@uptc.edu.co",
                java.util.Map.of("correoInstitucional", "ana.torres@uptc.edu.co"));
    }
}
