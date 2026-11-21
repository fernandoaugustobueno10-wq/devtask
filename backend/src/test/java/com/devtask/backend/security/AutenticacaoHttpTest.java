
package com.devtask.backend.security;

import com.devtask.backend.config.SecurityConfig;
import com.devtask.backend.controller.TarefaController;
import com.devtask.backend.service.TarefaService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@Import({
        SecurityConfig.class,
        AutenticacaoHttpTest.UsuarioTesteConfig.class
})
class AutenticacaoHttpTest {

    private static final String EMAIL = "usuario@example.com";
    private static final String SENHA = "SenhaDeTeste123456";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaService tarefaService;

    @Test
    void devePermitirLoginComCredenciaisCorretas()
            throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", EMAIL)
                        .param("password", SENHA))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRejeitarLoginComSenhaIncorreta()
            throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", EMAIL)
                        .param("password", "SenhaIncorreta123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveBloquearLoginSemCsrf()
            throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .param("username", EMAIL)
                        .param("password", SENHA))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAcessoAutenticadoEInvalidarSessaoNoLogout()
            throws Exception {

        when(tarefaService.listarTodas())
                .thenReturn(Collections.emptyList());

        MvcResult resultadoLogin = mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .param("username", EMAIL)
                                .param("password", SENHA))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession sessao = (MockHttpSession)
                resultadoLogin.getRequest().getSession(false);

        assertNotNull(
                sessao,
                "O login deve criar uma sessao autenticada."
        );

        mockMvc.perform(get("/api/tarefas")
                        .session(sessao))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout")
                        .session(sessao)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertTrue(
                sessao.isInvalid(),
                "O logout deve invalidar a sessao autenticada."
        );
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class UsuarioTesteConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(4);
        }

        @Bean
        UserDetailsService userDetailsService(
                PasswordEncoder passwordEncoder) {

            UserDetails usuario = User.withUsername(EMAIL)
                    .password(passwordEncoder.encode(SENHA))
                    .roles("USER")
                    .build();

            return new InMemoryUserDetailsManager(usuario);
        }
    }
}
