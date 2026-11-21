
package com.devtask.backend.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorExpiracaoTest {

    private static final String IP = "192.0.2.210";
    private static final String EMAIL = "usuario@example.com";

    @Test
    void deveLiberarLoginAposQuinzeMinutosDeBloqueio() {

        RelogioControlavel relogio = new RelogioControlavel(
                Instant.parse("2026-11-21T12:00:00Z")
        );

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin(relogio);

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            UUID reserva = limitador.reservarTentativaIdentificada(
                    IP,
                    EMAIL
            );

            assertNotNull(reserva);

            limitador.finalizarTentativaComFalha(
                    IP,
                    EMAIL,
                    reserva
            );
        }

        assertNull(
                limitador.reservarTentativaIdentificada(IP, EMAIL)
        );

        assertTrue(
                limitador.segundosRestantes(IP, EMAIL) > 0
        );

        relogio.avancarSegundos(900);

        assertEquals(
                0,
                limitador.segundosRestantes(IP, EMAIL)
        );

        assertNotNull(
                limitador.reservarTentativaIdentificada(IP, EMAIL),
                "O login deve ser liberado apos o bloqueio."
        );
    }

    @Test
    void deveLiberarCapacidadeQuandoRegistrosExpiram() {

        RelogioControlavel relogio = new RelogioControlavel(
                Instant.parse("2026-11-21T12:00:00Z")
        );

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin(relogio);

        for (int indice = 0; indice < 10000; indice++) {
            UUID reserva = limitador.reservarTentativaIdentificada(
                    IP,
                    "usuario" + indice + "@example.com"
            );

            assertNotNull(
                    reserva,
                    "Deve aceitar os registros ate a capacidade."
            );
        }

        assertNull(
                limitador.reservarTentativaIdentificada(
                        IP,
                        "novo@example.com"
                ),
                "Uma nova combinacao deve ser recusada."
        );

        relogio.avancarSegundos(901);

        assertNotNull(
                limitador.reservarTentativaIdentificada(
                        IP,
                        "novo@example.com"
                ),
                "Registros expirados devem liberar capacidade."
        );
    }

    @Test
    void conclusaoAntigaNaoDeveAlterarNovaJanela() {

        RelogioControlavel relogio = new RelogioControlavel(
                Instant.parse("2026-11-21T12:00:00Z")
        );

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin(relogio);

        UUID reservaAntiga =
                limitador.reservarTentativaIdentificada(
                        IP,
                        EMAIL
                );

        assertNotNull(reservaAntiga);

        relogio.avancarSegundos(901);

        UUID reservaNova =
                limitador.reservarTentativaIdentificada(
                        IP,
                        EMAIL
                );

        assertNotNull(reservaNova);

        limitador.finalizarTentativaComFalha(
                IP,
                EMAIL,
                reservaAntiga
        );

        for (int tentativa = 0; tentativa < 3; tentativa++) {
            UUID reserva = limitador.reservarTentativaIdentificada(
                    IP,
                    EMAIL
            );

            assertNotNull(reserva);

            limitador.finalizarTentativaComFalha(
                    IP,
                    EMAIL,
                    reserva
            );
        }

        UUID ultimaReserva =
                limitador.reservarTentativaIdentificada(
                        IP,
                        EMAIL
                );

        assertNotNull(
                ultimaReserva,
                "Uma falha antiga nao deve consumir "
                        + "a capacidade da nova janela."
        );

        assertNull(
                limitador.reservarTentativaIdentificada(IP, EMAIL),
                "A nova janela deve respeitar cinco reservas."
        );

        limitador.finalizarTentativaComSucesso(
                IP,
                EMAIL,
                reservaAntiga
        );

        assertNull(
                limitador.reservarTentativaIdentificada(IP, EMAIL),
                "Um sucesso antigo nao deve apagar reservas novas."
        );

        limitador.finalizarTentativaComFalha(
                IP,
                EMAIL,
                reservaNova
        );

        limitador.finalizarTentativaComFalha(
                IP,
                EMAIL,
                ultimaReserva
        );

        assertFalse(
                limitador.permitirTentativa(IP, EMAIL)
        );
    }

    private static final class RelogioControlavel extends Clock {

        private Instant instante;

        private RelogioControlavel(Instant instanteInicial) {
            this.instante = instanteInicial;
        }

        private void avancarSegundos(long segundos) {
            instante = instante.plusSeconds(segundos);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return instante;
        }
    }
}
