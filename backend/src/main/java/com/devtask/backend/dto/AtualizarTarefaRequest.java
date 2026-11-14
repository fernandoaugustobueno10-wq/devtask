
package com.devtask.backend.dto;

import com.devtask.backend.model.PrioridadeTarefa;
import com.devtask.backend.model.StatusTarefa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarTarefaRequest(

        @NotBlank
        @Size(max = 120)
        String titulo,

        @Size(max = 2000)
        String descricao,

        @NotNull
        StatusTarefa status,

        @NotNull
        PrioridadeTarefa prioridade

) {
}
