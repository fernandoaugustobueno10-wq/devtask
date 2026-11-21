
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@Import({
        SecurityConfig.class,
        LimiteLoginPorIpHttpTest.UsuariosTesteConfig.class
})
class LimiteLoginPorIpHttpTest {

    private static final String IP_TESTE_BLOQUEIO =
            "192.0.2.230";

    private static final String IP_TESTE_ISOLAMENTO =
            "192.0.2.231";

    private static final String IP_ALTERNATIVO =
            "192.0.2.232";

    private static final String SENHA_INCORRETA =
            "SenhaIncorreta123!";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaService tarefaService;

    @Test
    void deveBloquearIpAposCemFalhasComEmailsDiferentes()
            throws Exception {

        for (int tentativa = 0; tentativa < 99; tentativa++) {

            String email =
                    "usuario" + tentativa + "@example.com";

            mockMvc.perform(
                    post("/api/auth/login")
                            .with(csrf())
                            .with(request -> {
                                request.setRemoteAddr(IP_TESTE_BLOQUEIO);
                                return request;
                            })
                            .param("username", email)
                            .param("password", SENHA_INCORRETA)
            ).andExpect(status().isUnauthorized());
        }

        mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(IP_TESTE_BLOQUEIO);
                            return request;
                        })
                        .param(
                                "username",
                                "usuario99@example.com"
                        )
                        .param("password", SENHA_INCORRETA)
        )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(
                        header().string(
                                "Cache-Control",
                                "no-store"
                        )
                );

        mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(IP_TESTE_BLOQUEIO);
                            return request;
                        })
                        .param(
                                "username",
                                "usuario100@example.com"
                        )
                        .param("password", SENHA_INCORRETA)
        )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void devePermitirOutroIpMesmoComPrimeiroBloqueado()
            throws Exception {

        for (int tentativa = 0; tentativa < 100; tentativa++) {

            String email =
                    "conta" + tentativa + "@example.com";

            mockMvc.perform(
                    post("/api/auth/login")
                            .with(csrf())
                            .with(request -> {
                                request.setRemoteAddr(
                                        IP_TESTE_ISOLAMENTO
                                );
                                return request;
                            })
                            .param("username", email)
                            .param("password", SENHA_INCORRETA)
            );
        }

        mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(
                                    IP_TESTE_ISOLAMENTO
                                );
                                return request;
                            })
                        .param(
                                "username",
                                "conta100@example.com"
                        )
                        .param("password", SENHA_INCORRETA)
        ).andExpect(status().isTooManyRequests());

        mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(IP_ALTERNATIVO);
                            return request;
                        })
                        .param(
                                "username",
                                "outra-conta@example.com"
                        )
                        .param("password", SENHA_INCORRETA)
        ).andExpect(status().isUnauthorized());
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

            UserDetails usuario = User.withUsername(
                            "usuario-teste@example.com"
                    )
                    .password(
                            passwordEncoder.encode(
                                    "SenhaSegura123!"
                            )
                    )
                    .roles("USER")
                    .build();

            return new InMemoryUserDetailsManager(usuario);
        }
    }
}
