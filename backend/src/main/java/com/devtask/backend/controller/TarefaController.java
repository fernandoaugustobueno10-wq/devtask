
package com.devtask.backend.controller;

import com.devtask.backend.dto.CriarTarefaRequest;
import com.devtask.backend.model.Tarefa;
import com.devtask.backend.service.TarefaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @GetMapping
    public List<Tarefa> listarTarefas() {
        return tarefaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarTarefaPorId(
            @PathVariable Long id) {

        return tarefaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tarefa cadastrarTarefa(
            @Valid @RequestBody CriarTarefaRequest request) {

        return tarefaService.cadastrar(request);
    }
}
