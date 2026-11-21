
package com.devtask.backend.security;

import com.devtask.backend.config.FiltroLimiteLogin;
import com.devtask.backend.service.LimitadorTentativasLogin;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiltroLimiteLoginExcecaoTest {

    private static final String IP = "192.0.2.201";
    private static final String EMAIL = "usuario@example.com";

    @Test
    void deveLiberarReservaQuandoCadeiaDeFiltrosFalha() {
        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin();

        FiltroLimiteLogin filtro =
                new FiltroLimiteLogin(limitador);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr(IP);
        request.addParameter("username", EMAIL);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertThrows(
                ServletException.class,
                () -> filtro.doFilter(
                        request,
                        response,
                        (requisicao, resposta) -> {
                            throw new ServletException(
                                    "Falha simulada"
                            );
                        }
                )
        );

        UUID[] reservas = new UUID[5];

        for (int indice = 0; indice < reservas.length; indice++) {
            reservas[indice] =
                    limitador.reservarTentativaIdentificada(
                            IP,
                            EMAIL
                    );

            assertNotNull(reservas[indice]);
        }
    }
}
