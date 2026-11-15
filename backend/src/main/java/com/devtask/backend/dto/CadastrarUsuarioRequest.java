
package com.devtask.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarUsuarioRequest(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(
                max = 120,
                message = "O nome deve ter no maximo 120 caracteres"
        )
        String nome,

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Informe um email valido")
        @Size(
                max = 254,
                message = "O email deve ter no maximo 254 caracteres"
        )
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(
                min = 12,
                message = "A senha deve ter no minimo 12 caracteres"
        )
        String senha

) {
}

