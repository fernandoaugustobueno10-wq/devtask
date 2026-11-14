
package com.devtask.backend.service;

import com.devtask.backend.dto.AtualizarTarefaRequest;
import com.devtask.backend.dto.CriarTarefaRequest;
import com.devtask.backend.model.StatusTarefa;
import com.devtask.backend.model.Tarefa;
import com.devtask.backend.repository.TarefaRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TarefaService {

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
