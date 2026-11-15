
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    private TarefaService tarefaService;

    @BeforeEach
    void configurar() {
        tarefaService = new TarefaService(tarefaRepository);
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

        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    void deveBuscarTarefaExistente() {
        Tarefa tarefa = criarTarefa();

        when(tarefaRepository.findById(1L))
                .thenReturn(Optional.of(tarefa));

        Optional<Tarefa> resultado = tarefaService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Estudar Java", resultado.get().getTitulo());

        verify(tarefaRepository).findById(1L);
    }

    @Test
    void deveRetornarVazioQuandoTarefaNaoExiste() {
        when(tarefaRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<Tarefa> resultado = tarefaService.buscarPorId(999L);

        assertTrue(resultado.isEmpty());

        verify(tarefaRepository).findById(999L);
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

        AtualizarTarefaRequest request = new AtualizarTarefaRequest(
                "Estudar Spring Boot",
                "Revisar API REST",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA
        );

        when(tarefaRepository.findById(1L))
                .thenReturn(Optional.of(tarefa));

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Tarefa> resultado = tarefaService.atualizar(
                1L,
                request
        );

        assertTrue(resultado.isPresent());
        assertEquals("Estudar Spring Boot", resultado.get().getTitulo());
        assertEquals("Revisar API REST", resultado.get().getDescricao());
        assertEquals(StatusTarefa.EM_ANDAMENTO, resultado.get().getStatus());
        assertEquals(PrioridadeTarefa.ALTA, resultado.get().getPrioridade());

        verify(tarefaRepository).findById(1L);
        verify(tarefaRepository).save(tarefa);
    }

    @Test
    void naoDeveAtualizarTarefaInexistente() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest(
                "Nova tarefa",
                "Descricao",
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.MEDIA
        );

        when(tarefaRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<Tarefa> resultado = tarefaService.atualizar(
                999L,
                request
        );

        assertTrue(resultado.isEmpty());

        verify(tarefaRepository).findById(999L);
        verify(tarefaRepository, never()).save(any(Tarefa.class));
    }

    @Test
    void naoDeveAtualizarComIdInvalido() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest(
                "Nova tarefa",
                "Descricao",
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.MEDIA
        );

        Optional<Tarefa> resultado = tarefaService.atualizar(
                -1L,
                request
        );

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void deveExcluirTarefaExistente() {
        Tarefa tarefa = criarTarefa();

        when(tarefaRepository.findById(1L))
                .thenReturn(Optional.of(tarefa));

        boolean resultado = tarefaService.excluir(1L);

        assertTrue(resultado);

        verify(tarefaRepository).findById(1L);
        verify(tarefaRepository).delete(tarefa);
    }

    @Test
    void naoDeveExcluirTarefaInexistente() {
        when(tarefaRepository.findById(999L))
                .thenReturn(Optional.empty());

        boolean resultado = tarefaService.excluir(999L);

        assertFalse(resultado);

        verify(tarefaRepository).findById(999L);
        verify(tarefaRepository, never()).delete(any(Tarefa.class));
    }

    @Test
    void naoDeveExcluirComIdInvalido() {
        boolean resultado = tarefaService.excluir(-1L);

        assertFalse(resultado);

        verifyNoInteractions(tarefaRepository);
    }

    private Tarefa criarTarefa() {
        return new Tarefa(
                1L,
                "Estudar Java",
                "Revisar Spring Boot",
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.MEDIA,
                LocalDateTime.now()
        );
    }
}
