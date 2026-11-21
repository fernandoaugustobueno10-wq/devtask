
package com.devtask.backend.security;

import com.devtask.backend.config.SecurityConfig;
import com.devtask.backend.controller.TarefaController;
import com.devtask.backend.service.TarefaService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@Import(SecurityConfig.class)
class SecurityLogoutTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaService tarefaService;

    @Test
    @WithMockUser(username = "usuario@example.com")
    void devePermitirLogoutComCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(header().string(
                        "Cache-Control",
                        "no-store"
                ));
    }

    @Test
    @WithMockUser(username = "usuario@example.com")
    void deveBloquearLogoutSemCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "usuario@example.com")
    void deveInvalidarSessaoNoLogout() throws Exception {
        MockHttpSession sessao = new MockHttpSession();

        mockMvc.perform(post("/api/auth/logout")
                        .session(sessao)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertTrue(
                sessao.isInvalid(),
                "A sessao deve ser invalidada apos o logout."
        );
    }

    @Test
    void deveBloquearAcessoSemAutenticacaoAposLogout()
            throws Exception {

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
