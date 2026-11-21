
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

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@Import({
        SecurityConfig.class,
        LimiteLoginCsrfTest.UsuariosTesteConfig.class
})
class LimiteLoginCsrfTest {

    private static final String EMAIL = "csrf@example.com";
    private static final String SENHA_CORRETA = "SenhaSegura123!";
    private static final String SENHA_INCORRETA = "SenhaErrada123!";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaService tarefaService;

    @Test
    void requisicoesSemCsrfNaoDevemConsumirTentativas()
            throws Exception {

        String ip = "192.0.2.150";

        for (int tentativa = 0; tentativa < 10; tentativa++) {
            mockMvc.perform(
                    post("/api/auth/login")
                            .with(request -> {
                                request.setRemoteAddr(ip);
                                return request;
                            })
                            .param("username", EMAIL)
                            .param("password", SENHA_INCORRETA)
            ).andExpect(status().isForbidden());
        }

        for (int tentativa = 0; tentativa < 4; tentativa++) {
            mockMvc.perform(
                    post("/api/auth/login")
                            .with(csrf())
                            .with(request -> {
                                request.setRemoteAddr(ip);
                                return request;
                            })
                            .param("username", EMAIL)
                            .param("password", SENHA_INCORRETA)
            ).andExpect(status().isUnauthorized());
        }

        mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .param("username", EMAIL)
                        .param("password", SENHA_INCORRETA)
        ).andExpect(status().isTooManyRequests());
    }

    @Test
    void csrfInvalidoNaoDeveBloquearLoginValido()
            throws Exception {

        String ip = "192.0.2.151";

        for (int tentativa = 0; tentativa < 10; tentativa++) {
            mockMvc.perform(
                    post("/api/auth/login")
                            .with(csrf().useInvalidToken())
                            .with(request -> {
                                request.setRemoteAddr(ip);
                                return request;
                            })
                            .param("username", EMAIL)
                            .param("password", SENHA_INCORRETA)
            ).andExpect(status().isForbidden());
        }

        mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .param("username", EMAIL)
                        .param("password", SENHA_CORRETA)
        ).andExpect(status().isNoContent());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class UsuariosTesteConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(4);
        }

        @Bean
        UserDetailsService userDetailsService(
                PasswordEncoder passwordEncoder) {

            UserDetails usuario = User.withUsername(EMAIL)
                    .password(
                            passwordEncoder.encode(SENHA_CORRETA)
                    )
                    .roles("USER")
                    .build();

            return new InMemoryUserDetailsManager(usuario);
        }
    }
}
