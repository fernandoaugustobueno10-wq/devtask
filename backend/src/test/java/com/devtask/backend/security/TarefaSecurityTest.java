
package com.devtask.backend.security;

import com.devtask.backend.config.SecurityConfig;
import com.devtask.backend.controller.TarefaController;
import com.devtask.backend.service.TarefaService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@Import(SecurityConfig.class)
class TarefaSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaService tarefaService;

    @Test
    void deveExigirAutenticacaoParaListarTarefas() throws Exception {
        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(tarefaService);
    }

    @Test
    @WithMockUser(username = "usuario@example.com")
    void devePermitirListagemParaUsuarioAutenticado() throws Exception {
        when(tarefaService.listarTodas())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(tarefaService).listarTodas();
    }

    @Test
    @WithMockUser(username = "usuario@example.com")
    void deveBloquearCadastroSemCsrf() throws Exception {
        String dados = """
                {
                    "titulo": "Teste de seguranca",
                    "descricao": "Verificacao de CSRF",
                    "prioridade": "BAIXA"
                }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tarefaService);
    }

    @Test
    @WithMockUser(username = "usuario@example.com")
    void devePermitirCadastroComCsrf() throws Exception {
        String dados = """
                {
                    "titulo": "Teste de seguranca",
                    "descricao": "Verificacao de CSRF",
                    "prioridade": "BAIXA"
                }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isCreated());

        verify(tarefaService).cadastrar(any());
    }

    @Test
    @WithMockUser(username = "usuario@example.com")
    void deveBloquearEdicaoSemCsrf() throws Exception {
        String dados = """
                {
                    "titulo": "Tentativa de edicao",
                    "descricao": "Sem token CSRF",
                    "status": "PENDENTE",
                    "prioridade": "BAIXA"
                }
                """;

        mockMvc.perform(put("/api/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tarefaService);
    }

    @Test
    @WithMockUser(username = "usuario@example.com")
    void deveBloquearExclusaoSemCsrf() throws Exception {
        mockMvc.perform(delete("/api/tarefas/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tarefaService);
    }

    @Test
    void deveBloquearCadastroNaoAutenticadoMesmoComCsrf()
            throws Exception {

        String dados = """
                {
                    "titulo": "Tentativa sem login",
                    "descricao": "Usuario nao autenticado",
                    "prioridade": "BAIXA"
                }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(tarefaService);
    }
}
