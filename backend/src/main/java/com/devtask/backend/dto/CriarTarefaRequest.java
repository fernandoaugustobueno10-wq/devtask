
package com.devtask.backend.dto;

import com.devtask.backend.model.PrioridadeTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarTarefaRequest(
        @NotBlank
        @Size(max = 120)
        String titulo,

        @Size(max = 2000)
        String descricao,

        @NotNull
        PrioridadeTarefa prioridade
) {
}
