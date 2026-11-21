
package com.devtask.backend.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorReservasPendentesTest {

    private static final String IP = "192.0.2.200";
    private static final String EMAIL = "usuario@example.com";

    @Test
    void loginBemSucedidoNaoDeveApagarOutrasReservasPendentes() {

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin();

        UUID[] identificadores = new UUID[5];

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            identificadores[tentativa] =
                    limitador.reservarTentativaIdentificada(
                            IP,
                            EMAIL
                    );

            assertNotNull(
                    identificadores[tentativa],
                    "As cinco reservas iniciais devem ser aceitas."
            );
        }

        assertNull(
                limitador.reservarTentativaIdentificada(IP, EMAIL),
                "Uma sexta reserva deve ser recusada."
        );

        limitador.finalizarTentativaComSucesso(
                IP,
                EMAIL,
                identificadores[0]
        );

        UUID novaReserva =
                limitador.reservarTentativaIdentificada(
                        IP,
                        EMAIL
                );

        assertNotNull(
                novaReserva,
                "O sucesso deve liberar apenas sua propria reserva."
        );

        assertNull(
                limitador.reservarTentativaIdentificada(IP, EMAIL),
                "As outras quatro reservas continuam pendentes."
        );

        for (int indice = 1; indice < 5; indice++) {
            limitador.finalizarTentativaComFalha(
                    IP,
                    EMAIL,
                    identificadores[indice]
            );
        }

        limitador.finalizarTentativaComFalha(
                IP,
                EMAIL,
                novaReserva
        );

        assertFalse(
                limitador.permitirTentativa(IP, EMAIL),
                "Cinco falhas devem bloquear o login."
        );

        assertTrue(
                limitador.segundosRestantes(IP, EMAIL) > 0
        );
    }

    @Test
    void mesmaReservaNaoDeveSerFinalizadaDuasVezes() {

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin();

        UUID identificador =
                limitador.reservarTentativaIdentificada(
                        IP,
                        EMAIL
                );

        assertNotNull(identificador);

        limitador.finalizarTentativaComFalha(
                IP,
                EMAIL,
                identificador
        );

        limitador.finalizarTentativaComFalha(
                IP,
                EMAIL,
                identificador
        );

        for (int tentativa = 0; tentativa < 3; tentativa++) {
            UUID novaReserva =
                    limitador.reservarTentativaIdentificada(
                            IP,
                            EMAIL
                    );

            assertNotNull(novaReserva);

            limitador.finalizarTentativaComFalha(
                    IP,
                    EMAIL,
                    novaReserva
            );
        }

        UUID quintaReserva =
                limitador.reservarTentativaIdentificada(
                        IP,
                        EMAIL
                );

        assertNotNull(
                quintaReserva,
                "A repeticao de uma finalizacao nao pode "
                        + "contar como uma falha adicional."
        );

        limitador.finalizarTentativaComFalha(
                IP,
                EMAIL,
                quintaReserva
        );

        assertNull(
                limitador.reservarTentativaIdentificada(IP, EMAIL),
                "O bloqueio deve acontecer apos cinco falhas."
        );
    }
}
