
package com.devtask.backend.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorCapacidadeTest {

    private static final String IP = "192.0.2.100";
    private static final String EMAIL_BLOQUEADO =
            "bloqueado@example.com";

    @Test
    void naoDeveDescartarBloqueioAtivoQuandoAtingirCapacidade() {
        Clock relogio = Clock.fixed(
                Instant.parse("2026-11-20T12:00:00Z"),
                ZoneOffset.UTC
        );

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin(relogio);

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            assertTrue(
                    limitador.reservarTentativa(
                            IP,
                            EMAIL_BLOQUEADO
                    )
            );
        }

        assertFalse(
                limitador.reservarTentativa(
                        IP,
                        EMAIL_BLOQUEADO
                )
        );

        for (int indice = 0; indice < 9999; indice++) {
            String email = "usuario" + indice + "@example.com";

            assertTrue(
                    limitador.reservarTentativa(IP, email)
            );
        }

        assertFalse(
                limitador.reservarTentativa(
                        IP,
                        "usuario-extra@example.com"
                )
        );

        assertFalse(
                limitador.permitirTentativa(
                        IP,
                        EMAIL_BLOQUEADO
                ),
                "O bloqueio ativo nao pode ser removido."
        );
    }

    @Test
    void deveRemoverRegistrosExpiradosParaLiberarCapacidade() {
        Clock relogioInicial = Clock.fixed(
                Instant.parse("2026-11-20T12:00:00Z"),
                ZoneOffset.UTC
        );

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin(relogioInicial);

        for (int indice = 0; indice < 10000; indice++) {
            String email = "usuario" + indice + "@example.com";

            assertTrue(
                    limitador.reservarTentativa(IP, email)
            );
        }

        assertFalse(
                limitador.reservarTentativa(
                        IP,
                        "novo@example.com"
                )
        );
    }
}
