
package com.devtask.backend.controller;

import com.devtask.backend.dto.CadastrarUsuarioRequest;
import com.devtask.backend.dto.CadastrarUsuarioResponse;
import com.devtask.backend.exception.EmailJaCadastradoException;
import com.devtask.backend.model.Usuario;
import com.devtask.backend.service.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    private UsuarioController usuarioController;

    @BeforeEach
    void configurar() {
        usuarioController = new UsuarioController(usuarioService);
    }

    @Test
    void deveCadastrarUsuarioSemExporSenha() {
        CadastrarUsuarioRequest request =
                new CadastrarUsuarioRequest(
                        "Usuario Teste",
                        "teste@example.com",
                        "SenhaSegura123456"
                );

        Usuario usuario = new Usuario(
                "Usuario Teste",
                "teste@example.com",
                "$2a$12$hashFicticio"
        );

        LocalDateTime dataCriacao = LocalDateTime.now();

        ReflectionTestUtils.setField(usuario, "id", 1L);
        ReflectionTestUtils.setField(
                usuario,
                "criadoEm",
                dataCriacao
        );

        when(usuarioService.cadastrar(request))
                .thenReturn(usuario);

        CadastrarUsuarioResponse resposta =
                usuarioController.cadastrar(request);

        assertEquals(1L, resposta.id());
        assertEquals("Usuario Teste", resposta.nome());
        assertEquals("teste@example.com", resposta.email());
        assertEquals(dataCriacao, resposta.criadoEm());

        assertEquals(4,
                CadastrarUsuarioResponse.class
                        .getRecordComponents().length);

        verify(usuarioService).cadastrar(request);
    }

    @Test
    void devePropagarErroDeEmailDuplicado() {
        CadastrarUsuarioRequest request =
                new CadastrarUsuarioRequest(
                        "Usuario Teste",
                        "teste@example.com",
                        "SenhaSegura123456"
                );

        when(usuarioService.cadastrar(request))
                .thenThrow(new EmailJaCadastradoException());

        assertThrows(
                EmailJaCadastradoException.class,
                () -> usuarioController.cadastrar(request)
        );

        verify(usuarioService).cadastrar(request);
    }

    @Test
    void deveUsarOsDadosRetornadosPeloServico() {
        CadastrarUsuarioRequest request =
                new CadastrarUsuarioRequest(
                        "  Usuario Teste  ",
                        "TESTE@EXAMPLE.COM",
                        "SenhaSegura123456"
                );

        Usuario usuario = new Usuario(
                "Usuario Teste",
                "teste@example.com",
                "$2a$12$hashFicticio"
        );

        ReflectionTestUtils.setField(usuario, "id", 2L);
        ReflectionTestUtils.setField(
                usuario,
                "criadoEm",
                LocalDateTime.now()
        );

        when(usuarioService.cadastrar(request))
                .thenReturn(usuario);

        CadastrarUsuarioResponse resposta =
                usuarioController.cadastrar(request);

        assertEquals("Usuario Teste", resposta.nome());
        assertEquals("teste@example.com", resposta.email());
        assertEquals(2L, resposta.id());
    }
}
