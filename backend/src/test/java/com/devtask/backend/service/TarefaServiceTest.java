
package com.devtask.backend.service;

import com.devtask.backend.dto.AtualizarTarefaRequest;
import com.devtask.backend.dto.CriarTarefaRequest;
import com.devtask.backend.model.PrioridadeTarefa;
import com.devtask.backend.model.StatusTarefa;
import com.devtask.backend.model.Tarefa;
import com.devtask.backend.repository.TarefaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    private static final Long USUARIO_ID = 1L;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    private TarefaService tarefaService;

    @BeforeEach
    void configurar() {
        tarefaService = new TarefaService(
                tarefaRepository,
                usuarioAutenticadoService
        );

        lenient()
                .when(usuarioAutenticadoService.obterId())
                .thenReturn(USUARIO_ID);
    }

    @Test
    void deveCadastrarTarefaComStatusPendente() {
        CriarTarefaRequest request = new CriarTarefaRequest(
                "Estudar Java",
                "Revisar Spring Boot",
                PrioridadeTarefa.ALTA
        );

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenAnswer(invocation -> {
                    Tarefa tarefa = invocation.getArgument(0);
                    tarefa.setId(1L);
                    return tarefa;
                });

        Tarefa resultado = tarefaService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Estudar Java", resultado.getTitulo());
        assertEquals(StatusTarefa.PENDENTE, resultado.getStatus());
        assertEquals(PrioridadeTarefa.ALTA, resultado.getPrioridade());
        assertEquals(USUARIO_ID, resultado.getUsuarioId());

        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    void deveBuscarTarefaExistente() {
        Tarefa tarefa = criarTarefa();

        when(tarefaRepository.findByIdAndUsuarioId(1L, USUARIO_ID))
                .thenReturn(Optional.of(tarefa));

        Optional<Tarefa> resultado = tarefaService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Estudar Java", resultado.get().getTitulo());

        verify(tarefaRepository)
                .findByIdAndUsuarioId(1L, USUARIO_ID);
    }

    @Test
    void deveRetornarVazioQuandoTarefaNaoExiste() {
        when(tarefaRepository.findByIdAndUsuarioId(999L, USUARIO_ID))
                .thenReturn(Optional.empty());

        Optional<Tarefa> resultado = tarefaService.buscarPorId(999L);

        assertTrue(resultado.isEmpty());

        verify(tarefaRepository)
                .findByIdAndUsuarioId(999L, USUARIO_ID);
    }

    @Test
    void naoDeveConsultarBancoComIdInvalido() {
        Optional<Tarefa> resultado = tarefaService.buscarPorId(-1L);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void deveAtualizarTarefaExistente() {
        Tarefa tarefa = criarTarefa();

        AtualizarTarefaRequest request = criarAtualizacao();

        when(tarefaRepository.findByIdAndUsuarioId(1L, USUARIO_ID))
                .thenReturn(Optional.of(tarefa));

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Tarefa> resultado = tarefaService.atualizar(
                1L,
                request
        );

        assertTrue(resultado.isPresent());
        assertEquals("Estudar Spring Boot",
                resultado.get().getTitulo());
        assertEquals("Revisar API REST",
                resultado.get().getDescricao());
        assertEquals(StatusTarefa.EM_ANDAMENTO,
                resultado.get().getStatus());
        assertEquals(PrioridadeTarefa.ALTA,
                resultado.get().getPrioridade());

        verify(tarefaRepository)
                .findByIdAndUsuarioId(1L, USUARIO_ID);
        verify(tarefaRepository).save(tarefa);
    }

    @Test
    void naoDeveAtualizarTarefaInexistente() {
        when(tarefaRepository.findByIdAndUsuarioId(999L, USUARIO_ID))
                .thenReturn(Optional.empty());

        Optional<Tarefa> resultado = tarefaService.atualizar(
                999L,
                criarAtualizacao()
        );

        assertTrue(resultado.isEmpty());

        verify(tarefaRepository, never())
                .save(any(Tarefa.class));
    }

    @Test
    void naoDeveAtualizarComIdInvalido() {
        Optional<Tarefa> resultado = tarefaService.atualizar(
                -1L,
                criarAtualizacao()
        );

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void deveExcluirTarefaExistente() {
        Tarefa tarefa = criarTarefa();

        when(tarefaRepository.findByIdAndUsuarioId(1L, USUARIO_ID))
                .thenReturn(Optional.of(tarefa));

        boolean resultado = tarefaService.excluir(1L);

        assertTrue(resultado);

        verify(tarefaRepository)
                .findByIdAndUsuarioId(1L, USUARIO_ID);
        verify(tarefaRepository).delete(tarefa);
    }

    @Test
    void naoDeveExcluirTarefaInexistente() {
        when(tarefaRepository.findByIdAndUsuarioId(999L, USUARIO_ID))
                .thenReturn(Optional.empty());

        boolean resultado = tarefaService.excluir(999L);

        assertFalse(resultado);

        verify(tarefaRepository, never())
                .delete(any(Tarefa.class));
    }

    @Test
    void naoDeveExcluirComIdInvalido() {
        boolean resultado = tarefaService.excluir(-1L);

        assertFalse(resultado);
        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void deveListarSomenteTarefasDoUsuario() {
        Tarefa tarefa = criarTarefa();

        when(tarefaRepository.findAllByUsuarioId(
                eq(USUARIO_ID),
                any()
        )).thenReturn(List.of(tarefa));

        List<Tarefa> resultado = tarefaService.listarTodas();

        assertEquals(1, resultado.size());
        assertEquals(USUARIO_ID, resultado.get(0).getUsuarioId());

        verify(tarefaRepository)
                .findAllByUsuarioId(eq(USUARIO_ID), any());

        verify(tarefaRepository, never()).findAll();
    }

    @Test
    void naoDeveAtualizarTarefaDeOutroUsuario() {
        when(tarefaRepository.findByIdAndUsuarioId(2L, USUARIO_ID))
                .thenReturn(Optional.empty());

        Optional<Tarefa> resultado = tarefaService.atualizar(
                2L,
                criarAtualizacao()
        );

        assertTrue(resultado.isEmpty());

        verify(tarefaRepository, never())
                .save(any(Tarefa.class));
    }

    @Test
    void naoDeveExcluirTarefaDeOutroUsuario() {
        when(tarefaRepository.findByIdAndUsuarioId(2L, USUARIO_ID))
                .thenReturn(Optional.empty());

        boolean resultado = tarefaService.excluir(2L);

        assertFalse(resultado);

        verify(tarefaRepository, never())
                .delete(any(Tarefa.class));
    }

    private AtualizarTarefaRequest criarAtualizacao() {
        return new AtualizarTarefaRequest(
                "Estudar Spring Boot",
                "Revisar API REST",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA
        );
    }

    private Tarefa criarTarefa() {
        Tarefa tarefa = new Tarefa(
                1L,
                "Estudar Java",
                "Revisar Spring Boot",
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.MEDIA,
                LocalDateTime.now()
        );

        tarefa.atribuirProprietario(USUARIO_ID);

        return tarefa;
    }
}
