
package com.devtask.backend.service;

import com.devtask.backend.model.PrioridadeTarefa;
import com.devtask.backend.model.StatusTarefa;
import com.devtask.backend.model.Tarefa;
import com.devtask.backend.repository.TarefaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaConsultaTest {

    @Mock
    private TarefaRepository tarefaRepository;

    private TarefaService tarefaService;

    @BeforeEach
    void configurar() {
        tarefaService = new TarefaService(tarefaRepository);
    }

    @Test
    void deveConsultarTarefasComPaginacao() {
        Tarefa tarefa = new Tarefa(
                1L,
                "Estudar PostgreSQL",
                "Aprender Spring Data JPA",
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.ALTA,
                LocalDateTime.now()
        );

        Page<Tarefa> pagina = new PageImpl<>(List.of(tarefa));

        when(tarefaRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(pagina);

        Page<Tarefa> resultado = tarefaService.consultar(
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.ALTA,
                0,
                10,
                "id",
                Sort.Direction.ASC
        );

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Estudar PostgreSQL",
                resultado.getContent().get(0).getTitulo());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(tarefaRepository).findAll(
                any(Specification.class),
                captor.capture()
        );

        Pageable paginacao = captor.getValue();

        assertEquals(0, paginacao.getPageNumber());
        assertEquals(10, paginacao.getPageSize());
        assertTrue(paginacao.getSort().getOrderFor("id").isAscending());
    }

    @Test
    void naoDeveAceitarPaginaNegativa() {
        assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.consultar(
                        null,
                        null,
                        -1,
                        10,
                        "id",
                        Sort.Direction.ASC
                )
        );

        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void naoDeveAceitarTamanhoZero() {
        assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.consultar(
                        null,
                        null,
                        0,
                        0,
                        "id",
                        Sort.Direction.ASC
                )
        );

        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void naoDeveAceitarTamanhoMaiorQueCem() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.consultar(
                        null,
                        null,
                        0,
                        101,
                        "id",
                        Sort.Direction.ASC
                )
        );

        assertEquals(400, exception.getStatusCode().value());

        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void naoDeveAceitarCampoDeOrdenacaoInvalido() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.consultar(
                        null,
                        null,
                        0,
                        10,
                        "campoInexistente",
                        Sort.Direction.ASC
                )
        );

        assertEquals(400, exception.getStatusCode().value());

        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void naoDeveAceitarDirecaoNula() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.consultar(
                        null,
                        null,
                        0,
                        10,
                        "id",
                        null
                )
        );

        assertEquals(400, exception.getStatusCode().value());

        verifyNoInteractions(tarefaRepository);
    }

    @Test
    void devePermitirOrdenacaoDecrescente() {
        when(tarefaRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        tarefaService.consultar(
                null,
                null,
                0,
                20,
                "dataCriacao",
                Sort.Direction.DESC
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(tarefaRepository).findAll(
                any(Specification.class),
                captor.capture()
        );

        Pageable paginacao = captor.getValue();

        assertEquals(20, paginacao.getPageSize());

        Sort.Order ordem = paginacao.getSort()
                .getOrderFor("dataCriacao");

        assertNotNull(ordem);
        assertTrue(ordem.isDescending());
    }
}
