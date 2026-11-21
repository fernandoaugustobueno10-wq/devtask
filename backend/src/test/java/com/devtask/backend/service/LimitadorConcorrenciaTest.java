
package com.devtask.backend.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorConcorrenciaTest {

    private static final String IP = "192.0.2.100";
    private static final String EMAIL = "usuario@example.com";

    @Test
    void naoDevePermitirMaisDeCincoReservasSimultaneas()
            throws Exception {

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin();

        int quantidadeRequisicoes = 10;

        AtomicInteger tentativasPermitidas =
                new AtomicInteger();

        CountDownLatch iniciarTentativas =
                new CountDownLatch(1);

        CountDownLatch threadsProntas =
                new CountDownLatch(quantidadeRequisicoes);

        ExecutorService executor =
                Executors.newFixedThreadPool(quantidadeRequisicoes);

        try {
            @SuppressWarnings("unchecked")
            Future<?>[] tarefas =
                    new Future[quantidadeRequisicoes];

            for (int i = 0; i < quantidadeRequisicoes; i++) {
                tarefas[i] = executor.submit(() -> {
                    threadsProntas.countDown();

                    if (!iniciarTentativas.await(
                            5,
                            TimeUnit.SECONDS
                    )) {
                        throw new IllegalStateException(
                                "Tempo limite da sincronizacao excedido."
                        );
                    }

                    if (limitador.reservarTentativa(
                            IP,
                            EMAIL
                    )) {
                        tentativasPermitidas.incrementAndGet();
                    }

                    return null;
                });
            }

            assertTrue(
                    threadsProntas.await(
                            5,
                            TimeUnit.SECONDS
                    ),
                    "As threads nao ficaram prontas no prazo."
            );

            iniciarTentativas.countDown();

            for (Future<?> tarefa : tarefas) {
                tarefa.get(5, TimeUnit.SECONDS);
            }

            assertEquals(
                    5,
                    tentativasPermitidas.get(),
                    "O limitador deve aceitar exatamente "
                            + "cinco reservas simultaneas."
            );

            assertFalse(
                    limitador.reservarTentativa(IP, EMAIL),
                    "A sexta tentativa deve ser bloqueada."
            );

            assertTrue(
                    limitador.segundosRestantes(IP, EMAIL) > 0,
                    "O bloqueio deve possuir um tempo restante."
            );

        } finally {
            iniciarTentativas.countDown();
            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(
                            5,
                            TimeUnit.SECONDS
                    ),
                    "Nao foi possivel encerrar as threads."
            );
        }
    }

    @Test
    void deveLiberarNovasTentativasAposLoginBemSucedido() {
        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin();

        for (int tentativa = 0; tentativa < 4; tentativa++) {
            assertTrue(
                    limitador.reservarTentativa(IP, EMAIL)
            );
        }

        limitador.registrarSucesso(IP, EMAIL);

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            assertTrue(
                    limitador.reservarTentativa(IP, EMAIL)
            );
        }

        assertFalse(
                limitador.reservarTentativa(IP, EMAIL)
        );
    }
}
