
package com.devtask.backend.controller;

import com.devtask.backend.dto.CadastrarUsuarioRequest;
import com.devtask.backend.dto.CadastrarUsuarioResponse;
import com.devtask.backend.model.Usuario;
import com.devtask.backend.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(
        name = "Usuarios",
        description = "Operacoes relacionadas ao cadastro de usuarios"
)
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Cadastrar usuario",
            description = "Cadastra um novo usuario com a senha protegida "
                    + "por hash BCrypt."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de cadastro invalidos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email ja cadastrado"
            )
    })
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public CadastrarUsuarioResponse cadastrar(
            @Valid @RequestBody CadastrarUsuarioRequest request) {

        Usuario usuario = usuarioService.cadastrar(request);

        return CadastrarUsuarioResponse.from(usuario);
    }
}
