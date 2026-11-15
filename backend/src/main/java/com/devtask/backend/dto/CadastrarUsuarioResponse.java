
package com.devtask.backend.dto;

import com.devtask.backend.model.Usuario;

import java.time.LocalDateTime;

public record CadastrarUsuarioResponse(
        Long id,
        String nome,
        String email,
        LocalDateTime criadoEm
) {

    public static CadastrarUsuarioResponse from(Usuario usuario) {
        return new CadastrarUsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCriadoEm()
        );
    }
}
