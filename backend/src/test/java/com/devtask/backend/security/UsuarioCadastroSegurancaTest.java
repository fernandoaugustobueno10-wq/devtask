
package com.devtask.backend.security;

import com.devtask.backend.config.SecurityConfig;
import com.devtask.backend.controller.UsuarioController;
import com.devtask.backend.service.UsuarioService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioCadastroSegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void deveBloquearCadastroSemCsrf() throws Exception {
        String dados = """
                {
                    "nome": "Usuario Teste",
                    "email": "teste@example.com",
                    "senha": "MinhaSenhaSegura123"
                }
                """;

        mockMvc.perform(post("/api/usuarios/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRejeitarEmailInvalidoComCsrf() throws Exception {
        String dados = """
                {
                    "nome": "Usuario Teste",
                    "email": "email-invalido",
                    "senha": "MinhaSenhaSegura123"
                }
                """;

        mockMvc.perform(post("/api/usuarios/cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRejeitarSenhaCurtaComCsrf() throws Exception {
        String dados = """
                {
                    "nome": "Usuario Teste",
                    "email": "teste@example.com",
                    "senha": "123456"
                }
                """;

        mockMvc.perform(post("/api/usuarios/cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRejeitarNomeVazioComCsrf() throws Exception {
        String dados = """
                {
                    "nome": "   ",
                    "email": "teste@example.com",
                    "senha": "MinhaSenhaSegura123"
                }
                """;

        mockMvc.perform(post("/api/usuarios/cadastro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }
}
