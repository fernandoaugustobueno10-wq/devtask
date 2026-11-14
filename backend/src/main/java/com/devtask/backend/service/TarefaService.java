
package com.devtask.backend.service;

import com.devtask.backend.dto.CriarTarefaRequest;
import com.devtask.backend.model.StatusTarefa;
import com.devtask.backend.model.Tarefa;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TarefaService {

    private final ConcurrentHashMap<Long, Tarefa> tarefas =
            new ConcurrentHashMap<>();

    private final AtomicLong proximoId = new AtomicLong(1);

    public List<Tarefa> listarTodas() {
        return tarefas.values()
                .stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    public Tarefa cadastrar(CriarTarefaRequest request) {
        long id = proximoId.getAndIncrement();

        Tarefa tarefa = new Tarefa(
                id,
                request.titulo().trim(),
                request.descricao(),
                StatusTarefa.PENDENTE,
                request.prioridade(),
                LocalDateTime.now()
        );

        tarefas.put(id, tarefa);

        return tarefa;
    }
}
