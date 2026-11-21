
package com.devtask.backend.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorCapacidadeTest {

    private static final String IP_BLOQUEADO = "192.0.2.100";

    private static final String EMAIL_BLOQUEADO =
            "bloqueado@example.com";

    private static final int CAPACIDADE_MAXIMA = 10000;

    @Test
    void naoDeveDescartarBloqueioAtivoQuandoAtingirCapacidade() {

        Clock relogio = Clock.fixed(
                Instant.parse("2026-11-21T12:00:00Z"),
                ZoneOffset.UTC
        );

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin(relogio);

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            assertTrue(
                    limitador.reservarTentativa(
                            IP_BLOQUEADO,
                            EMAIL_BLOQUEADO
                    ),
                    "As cinco primeiras tentativas devem ser aceitas."
            );
        }

        assertFalse(
                limitador.reservarTentativa(
                        IP_BLOQUEADO,
                        EMAIL_BLOQUEADO
                ),
                "A sexta tentativa deve ser bloqueada."
        );

        for (int indice = 0;
                indice < CAPACIDADE_MAXIMA - 1;
                indice++) {

            String ip = gerarIpDeTeste(indice);

            String email =
                    "usuario" + indice + "@example.com";

            assertTrue(
                    limitador.reservarTentativa(
                            ip,
                            email
                    ),
                    "Deve aceitar o registro de indice " + indice
            );
        }

        assertFalse(
                limitador.reservarTentativa(
                        "203.0.113.250",
                        "usuario-extra@example.com"
                ),
                "Uma nova combinacao deve ser recusada "
                        + "quando a capacidade estiver esgotada."
        );

        assertFalse(
                limitador.permitirTentativa(
                        IP_BLOQUEADO,
                        EMAIL_BLOQUEADO
                ),
                "Um bloqueio ativo nao pode ser descartado "
                        + "para liberar espaco."
        );
    }

    @Test
    void deveRecusarNovosRegistrosQuandoCapacidadeEstiverEsgotada() {

        Clock relogio = Clock.fixed(
                Instant.parse("2026-11-21T12:00:00Z"),
                ZoneOffset.UTC
        );

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin(relogio);

        for (int indice = 0;
                indice < CAPACIDADE_MAXIMA;
                indice++) {

            String ip = gerarIpDeTeste(indice);

            String email =
                    "usuario" + indice + "@example.com";

            assertTrue(
                    limitador.reservarTentativa(
                            ip,
                            email
                    ),
                    "Deve aceitar o registro de indice " + indice
            );
        }

        assertFalse(
                limitador.reservarTentativa(
                        "203.0.113.251",
                        "novo@example.com"
                ),
                "O sistema nao deve ultrapassar "
                        + "a capacidade maxima de registros."
        );
    }

    private String gerarIpDeTeste(int indice) {

        if (indice < 0 || indice >= CAPACIDADE_MAXIMA) {
            throw new IllegalArgumentException(
                    "Indice de teste invalido."
            );
        }

        int terceiroOcteto = indice / 256;
        int quartoOcteto = indice % 256;

        return "198.18."
                + terceiroOcteto
                + "."
                + quartoOcteto;
    }
}
