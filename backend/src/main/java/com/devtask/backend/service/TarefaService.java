
package com.devtask.backend.service;

import com.devtask.backend.dto.AtualizarTarefaRequest;
import com.devtask.backend.dto.CriarTarefaRequest;
import com.devtask.backend.model.PrioridadeTarefa;
import com.devtask.backend.model.StatusTarefa;
import com.devtask.backend.model.Tarefa;
import com.devtask.backend.repository.TarefaRepository;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TarefaService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private static final Set<String> CAMPOS_ORDENACAO = Set.of(
            "id",
            "titulo",
            "status",
            "prioridade",
            "dataCriacao"
    );

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    @Transactional(readOnly = true)
    public List<Tarefa> listarTodas() {
        return tarefaRepository.findAll(
                Sort.by(Sort.Direction.ASC, "id")
        );
    }

    @Transactional(readOnly = true)
    public Page<Tarefa> consultar(
            StatusTarefa status,
            PrioridadeTarefa prioridade,
            int page,
            int size,
            String ordenarPor,
            Sort.Direction direcao) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A pagina deve ser maior ou igual a zero."
            );
        }

        if (size < 1 || size > TAMANHO_MAXIMO_PAGINA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tamanho da pagina deve estar entre 1 e 100."
            );
        }

        if (ordenarPor == null ||
                !CAMPOS_ORDENACAO.contains(ordenarPor)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campo de ordenacao invalido."
            );
        }

        if (direcao == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Direcao de ordenacao invalida."
            );
        }

        Sort ordenacao = Sort.by(direcao, ordenarPor);

        if (!"id".equals(ordenarPor)) {
            ordenacao = ordenacao.and(
                    Sort.by(Sort.Direction.ASC, "id")
            );
        }

        PageRequest paginacao = PageRequest.of(
                page,
                size,
                ordenacao
        );

        Specification<Tarefa> filtros = (root, query, builder) -> {
            List<Predicate> condicoes = new ArrayList<>();

            if (status != null) {
                condicoes.add(
                        builder.equal(root.get("status"), status)
                );
            }

            if (prioridade != null) {
                condicoes.add(
                        builder.equal(root.get("prioridade"), prioridade)
                );
            }

            return builder.and(
                    condicoes.toArray(new Predicate[0])
            );
        };

        return tarefaRepository.findAll(filtros, paginacao);
    }

    @Transactional(readOnly = true)
    public Optional<Tarefa> buscarPorId(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return tarefaRepository.findById(id);
    }

    @Transactional
    public Tarefa cadastrar(CriarTarefaRequest request) {
        Tarefa tarefa = new Tarefa(
                null,
                request.titulo().trim(),
                request.descricao(),
                StatusTarefa.PENDENTE,
                request.prioridade(),
                LocalDateTime.now()
        );

        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public Optional<Tarefa> atualizar(
            Long id,
            AtualizarTarefaRequest request) {

        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return tarefaRepository.findById(id)
                .map(tarefa -> {
                    tarefa.setTitulo(request.titulo().trim());
                    tarefa.setDescricao(request.descricao());
                    tarefa.setStatus(request.status());
                    tarefa.setPrioridade(request.prioridade());

                    return tarefaRepository.save(tarefa);
                });
    }

    @Transactional
    public boolean excluir(Long id) {
        if (id == null || id <= 0) {
            return false;
        }

        Optional<Tarefa> tarefa = tarefaRepository.findById(id);

        if (tarefa.isEmpty()) {
            return false;
        }

        tarefaRepository.delete(tarefa.get());

        return true;
    }
}
