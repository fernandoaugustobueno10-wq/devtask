
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@Import({
        SecurityConfig.class,
        LimiteLoginHttpTest.UsuariosTesteConfig.class
})
class LimiteLoginHttpTest {

    private static final String EMAIL =
            "usuario@example.com";

    private static final String SENHA_CORRETA =
            "SenhaSeguraTeste123";

    private static final String SENHA_INCORRETA =
            "SenhaErradaTeste123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaService tarefaService;

    @Test
    void deveBloquearNaQuintaFalhaEInformarTempoDeEspera()
            throws Exception {

        String ip = "192.0.2.10";

        for (int tentativa = 1; tentativa <= 4; tentativa++) {
            tentarLogin(ip, EMAIL, SENHA_INCORRETA)
                    .andExpect(status().isUnauthorized());
        }

        tentarLogin(ip, EMAIL, SENHA_INCORRETA)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string(
                        "Cache-Control",
                        "no-store"
                ));

        tentarLogin(ip, EMAIL, SENHA_CORRETA)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void deveLimparFalhasAposLoginCorreto()
            throws Exception {

        String ip = "192.0.2.20";

        for (int tentativa = 0; tentativa < 4; tentativa++) {
            tentarLogin(ip, EMAIL, SENHA_INCORRETA)
                    .andExpect(status().isUnauthorized());
        }

        tentarLogin(ip, EMAIL, SENHA_CORRETA)
                .andExpect(status().isNoContent());

        for (int tentativa = 0; tentativa < 4; tentativa++) {
            tentarLogin(ip, EMAIL, SENHA_INCORRETA)
                    .andExpect(status().isUnauthorized());
        }

        tentarLogin(ip, EMAIL, SENHA_INCORRETA)
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void deveManterLimitesSeparadosPorOrigemEEmail()
            throws Exception {

        String ipBloqueado = "192.0.2.30";
        String outroIp = "192.0.2.31";

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            tentarLogin(ipBloqueado, EMAIL, SENHA_INCORRETA);
        }

        tentarLogin(ipBloqueado, EMAIL, SENHA_CORRETA)
                .andExpect(status().isTooManyRequests());

        tentarLogin(outroIp, EMAIL, SENHA_CORRETA)
                .andExpect(status().isNoContent());

        tentarLogin(
                ipBloqueado,
                "outro@example.com",
                SENHA_INCORRETA
        ).andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions tentarLogin(
            String ip,
            String email,
            String senha) throws Exception {

        return mockMvc.perform(
                post("/api/auth/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .param("username", email)
                        .param("password", senha)
        );
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
