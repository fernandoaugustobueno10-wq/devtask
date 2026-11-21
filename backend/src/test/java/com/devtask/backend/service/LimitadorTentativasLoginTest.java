
package com.devtask.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorTentativasLoginTest {

    private static final String IP = "127.0.0.1";
    private static final String EMAIL = "usuario@example.com";

    private RelogioTeste relogio;
    private LimitadorTentativasLogin limitador;

    @BeforeEach
    void configurar() {
        relogio = new RelogioTeste(
                Instant.parse("2026-11-20T12:00:00Z")
        );

        limitador = new LimitadorTentativasLogin(relogio);
    }

    @Test
    void devePermitirTentativasAntesDoLimite() {
        for (int tentativa = 0; tentativa < 4; tentativa++) {
            assertTrue(limitador.permitirTentativa(IP, EMAIL));
            limitador.registrarFalha(IP, EMAIL);
        }

        assertTrue(limitador.permitirTentativa(IP, EMAIL));
    }

    @Test
    void deveBloquearAposCincoFalhas() {
        registrarFalhas(5);

        assertFalse(limitador.permitirTentativa(IP, EMAIL));

        assertTrue(
                limitador.segundosRestantes(IP, EMAIL) > 0
        );
    }

    @Test
    void deveLiberarAposQuinzeMinutos() {
        registrarFalhas(5);

        assertFalse(limitador.permitirTentativa(IP, EMAIL));

        relogio.avancar(Duration.ofMinutes(15));

        assertTrue(limitador.permitirTentativa(IP, EMAIL));

        assertEquals(
                0,
                limitador.segundosRestantes(IP, EMAIL)
        );
    }

    @Test
    void deveLimparFalhasAposSucesso() {
        registrarFalhas(4);

        limitador.registrarSucesso(IP, EMAIL);

        registrarFalhas(4);

        assertTrue(limitador.permitirTentativa(IP, EMAIL));
    }

    @Test
    void deveSepararTentativasPorEnderecoIp() {
        registrarFalhas(5);

        assertFalse(limitador.permitirTentativa(IP, EMAIL));

        assertTrue(
                limitador.permitirTentativa(
                        "127.0.0.2",
                        EMAIL
                )
        );
    }

    @Test
    void deveSepararTentativasPorEmail() {
        registrarFalhas(5);

        assertFalse(limitador.permitirTentativa(IP, EMAIL));

        assertTrue(
                limitador.permitirTentativa(
                        IP,
                        "outro@example.com"
                )
        );
    }

    @Test
    void deveNormalizarEmail() {
        registrarFalhas(5);

        assertFalse(
                limitador.permitirTentativa(
                        IP,
                        " USUARIO@EXAMPLE.COM "
                )
        );
    }

    @Test
    void deveReiniciarContagemAposJanelaExpirar() {
        registrarFalhas(4);

        relogio.avancar(Duration.ofMinutes(15));

        assertTrue(limitador.permitirTentativa(IP, EMAIL));

        registrarFalhas(4);

        assertTrue(limitador.permitirTentativa(IP, EMAIL));
    }

    private void registrarFalhas(int quantidade) {
        for (int tentativa = 0; tentativa < quantidade; tentativa++) {
            limitador.registrarFalha(IP, EMAIL);
        }
    }

    private static final class RelogioTeste extends Clock {

        private Instant instante;

        private RelogioTeste(Instant instante) {
            this.instante = instante;
        }

        private void avancar(Duration duracao) {
            instante = instante.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instante, zone);
        }

        @Override
        public Instant instant() {
            return instante;
        }
    }
}
