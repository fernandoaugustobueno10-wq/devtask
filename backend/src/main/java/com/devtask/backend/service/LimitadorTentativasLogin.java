
package com.devtask.backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LimitadorTentativasLogin {

    private static final int MAXIMO_TENTATIVAS = 5;
    private static final int MAXIMO_REGISTROS = 10000;
    private static final int MAXIMO_TAMANHO_EMAIL = 254;
    private static final int MAXIMO_TAMANHO_IP = 64;

    private static final Duration JANELA_TENTATIVAS =
            Duration.ofMinutes(15);

    private static final Duration DURACAO_BLOQUEIO =
            Duration.ofMinutes(15);

    private final Map<String, RegistroTentativas> registros =
            new HashMap<>();

    private final Clock clock;

    public LimitadorTentativasLogin() {
        this(Clock.systemUTC());
    }

    LimitadorTentativasLogin(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public synchronized boolean reservarTentativa(
            String enderecoIp,
            String email) {

        if (!permitirTentativa(enderecoIp, email)) {
            return false;
        }

        registrarFalha(enderecoIp, email);

        return true;
    }

    public synchronized boolean permitirTentativa(
            String enderecoIp,
            String email) {

        String chave = construirChave(enderecoIp, email);
        Instant agora = clock.instant();

        RegistroTentativas registro = registros.get(chave);

        if (registro != null && estaExpirado(registro, agora)) {
            registros.remove(chave);
            registro = null;
        }

        if (registro != null) {
            return registro.bloqueadoAte == null;
        }

        if (registros.size() >= MAXIMO_REGISTROS) {
            removerRegistrosExpirados(agora);

            if (registros.size() >= MAXIMO_REGISTROS) {
                return false;
            }
        }

        return true;
    }

    public synchronized void registrarFalha(
            String enderecoIp,
            String email) {

        String chave = construirChave(enderecoIp, email);
        Instant agora = clock.instant();

        RegistroTentativas registro = registros.get(chave);

        if (registro != null && estaExpirado(registro, agora)) {
            registros.remove(chave);
            registro = null;
        }

        if (registro == null) {
            if (registros.size() >= MAXIMO_REGISTROS) {
                removerRegistrosExpirados(agora);

                if (registros.size() >= MAXIMO_REGISTROS) {
                    return;
                }
            }

            registro = new RegistroTentativas(agora);
            registros.put(chave, registro);
        }

        if (registro.bloqueadoAte != null) {
            return;
        }

        registro.falhas++;

        if (registro.falhas >= MAXIMO_TENTATIVAS) {
            registro.bloqueadoAte =
                    agora.plus(DURACAO_BLOQUEIO);
        }
    }

    public synchronized void registrarSucesso(
            String enderecoIp,
            String email) {

        registros.remove(construirChave(enderecoIp, email));
    }

    public synchronized long segundosRestantes(
            String enderecoIp,
            String email) {

        String chave = construirChave(enderecoIp, email);
        RegistroTentativas registro = registros.get(chave);

        if (registro == null || registro.bloqueadoAte == null) {
            return 0;
        }

        Instant agora = clock.instant();

        if (!agora.isBefore(registro.bloqueadoAte)) {
            registros.remove(chave);
            return 0;
        }

        long milissegundos = Duration.between(
                agora,
                registro.bloqueadoAte
        ).toMillis();

        return Math.max(
                1,
                (milissegundos + 999L) / 1000L
        );
    }

    private boolean estaExpirado(
            RegistroTentativas registro,
            Instant agora) {

        if (registro.bloqueadoAte != null) {
            return !agora.isBefore(registro.bloqueadoAte);
        }

        return !agora.isBefore(
                registro.inicioJanela.plus(JANELA_TENTATIVAS)
        );
    }

    private void removerRegistrosExpirados(Instant agora) {
        registros.entrySet().removeIf(
                entrada -> estaExpirado(
                        entrada.getValue(),
                        agora
                )
        );
    }

    private String construirChave(
            String enderecoIp,
            String email) {

        String ipNormalizado = normalizarIp(enderecoIp);
        String emailNormalizado = normalizarEmail(email);

        return ipNormalizado.length()
                + ":"
                + ipNormalizado
                + ":"
                + emailNormalizado;
    }

    private String normalizarIp(String enderecoIp) {
        if (enderecoIp == null
                || enderecoIp.isBlank()
                || enderecoIp.length() > MAXIMO_TAMANHO_IP) {

            return "origem-desconhecida";
        }

        return enderecoIp.strip();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return "email-ausente";
        }

        String normalizado = email.strip()
                .toLowerCase(Locale.ROOT);

        if (normalizado.length() > MAXIMO_TAMANHO_EMAIL) {
            return "email-invalido";
        }

        return normalizado;
    }

    private static final class RegistroTentativas {

        private final Instant inicioJanela;
        private int falhas;
        private Instant bloqueadoAte;

        private RegistroTentativas(Instant inicioJanela) {
            this.inicioJanela = inicioJanela;
            this.falhas = 0;
        }
    }
}
