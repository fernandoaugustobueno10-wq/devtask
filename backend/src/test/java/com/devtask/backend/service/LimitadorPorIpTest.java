
package com.devtask.backend.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LimitadorPorIpTest {

    private static final String IP = "192.0.2.220";

    @Test
    void deveLimitarTentativasDoMesmoIpComEmailsDiferentes() {

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin();

        for (int tentativa = 0; tentativa < 100; tentativa++) {
            String email = "usuario" + tentativa + "@example.com";

            UUID reserva =
                    limitador.reservarTentativaIdentificada(
                            IP,
                            email
                    );

            assertNotNull(
                    reserva,
                    "As primeiras 100 tentativas devem ser aceitas."
            );

            limitador.finalizarTentativaComFalha(
                    IP,
                    email,
                    reserva
            );
        }

        assertNull(
                limitador.reservarTentativaIdentificada(
                        IP,
                        "usuario101@example.com"
                ),
                "O mesmo IP nao deve realizar tentativas "
                        + "ilimitadas alternando os emails."
        );
    }

    @Test
    void deveManterContagensSeparadasEntreIps() {

        LimitadorTentativasLogin limitador =
                new LimitadorTentativasLogin();

        String primeiroIp = "192.0.2.221";
        String segundoIp = "192.0.2.222";

        for (int tentativa = 0; tentativa < 100; tentativa++) {
            String email = "usuario" + tentativa + "@example.com";

            UUID reserva =
                    limitador.reservarTentativaIdentificada(
                            primeiroIp,
                            email
                    );

            assertNotNull(reserva);

            limitador.finalizarTentativaComFalha(
                    primeiroIp,
                    email,
                    reserva
            );
        }

        assertNotNull(
                limitador.reservarTentativaIdentificada(
                        segundoIp,
                        "outro@example.com"
                ),
                "O limite de um IP nao deve bloquear outro IP."
        );
    }
}
