package com.devtask.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    @GetMapping
    public Map<String, Object> listarTarefas() {
        return Map.of(
                "mensagem", "API DevTask funcionando!",
                "totalTarefas", 0
        );
    }
}