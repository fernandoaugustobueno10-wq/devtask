
package com.devtask.backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class LimitadorTentativasLogin {

    private static final int MAXIMO_TENTATIVAS = 5;
    private static final int MAXIMO_FALHAS_POR_IP = 100;
    private static final int MAXIMO_REGISTROS = 10000;
    private static final int MAXIMO_TAMANHO_EMAIL = 254;
    private static final int MAXIMO_TAMANHO_IP = 64;

    private static final Duration JANELA_TENTATIVAS =
            Duration.ofMinutes(15);

    private static final Duration DURACAO_BLOQUEIO =
            Duration.ofMinutes(15);

    private final Map<String, RegistroTentativas> registros =
            new HashMap<>();

    private final Map<String, RegistroIp> registrosPorIp =
            new HashMap<>();

    private final Clock clock;

    public LimitadorTentativasLogin() {
        this(Clock.systemUTC());
    }

    LimitadorTentativasLogin(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public synchronized UUID reservarTentativaIdentificada(
            String enderecoIp,
            String email) {

        Instant agora = clock.instant();
        String ip = normalizarIp(enderecoIp);
        String chave = construirChave(enderecoIp, email);

        if (!permitirPorIp(ip, agora)) {
            return null;
        }

        RegistroTentativas registro =
                obterRegistroValido(chave, agora);

        if (registro != null) {
            if (registro.bloqueadoAte != null) {
                return null;
            }

            if (registro.totalTentativas() >= MAXIMO_TENTATIVAS) {
                return null;
            }
        } else {
            if (!existeCapacidade(agora)) {
                return null;
            }

            registro = new RegistroTentativas(agora);
            registros.put(chave, registro);
        }

        UUID identificador = UUID.randomUUID();
        registro.reservasPendentes.add(identificador);

        return identificador;
    }

    public synchronized void finalizarTentativaComFalha(
            String enderecoIp,
            String email,
            UUID identificador) {

        if (identificador == null) {
            return;
        }

        String chave = construirChave(enderecoIp, email);
        Instant agora = clock.instant();

        RegistroTentativas registro =
                obterRegistroValido(chave, agora);

        if (registro == null) {
            return;
        }

        if (!registro.reservasPendentes.remove(identificador)) {
            return;
        }

        registro.falhas++;

        registrarFalhaPorIp(
                normalizarIp(enderecoIp),
                agora
        );

        if (registro.falhas >= MAXIMO_TENTATIVAS
                && registro.bloqueadoAte == null) {

            registro.bloqueadoAte =
                    agora.plus(DURACAO_BLOQUEIO);
        }
    }

    public synchronized void finalizarTentativaComSucesso(
            String enderecoIp,
            String email,
            UUID identificador) {

        if (identificador == null) {
            return;
        }

        String chave = construirChave(enderecoIp, email);
        Instant agora = clock.instant();

        RegistroTentativas registro =
                obterRegistroValido(chave, agora);

        if (registro == null) {
            return;
        }

        if (!registro.reservasPendentes.remove(identificador)) {
            return;
        }

        if (registro.bloqueadoAte != null) {
            return;
        }

        registro.falhas = 0;

        if (registro.reservasPendentes.isEmpty()) {
            registros.remove(chave);
        }
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

        Instant agora = clock.instant();
        String ip = normalizarIp(enderecoIp);

        if (!permitirPorIp(ip, agora)) {
            return false;
        }

        String chave = construirChave(enderecoIp, email);

        RegistroTentativas registro =
                obterRegistroValido(chave, agora);

        if (registro != null) {
            return registro.bloqueadoAte == null
                    && registro.totalTentativas() < MAXIMO_TENTATIVAS;
        }

        return existeCapacidade(agora);
    }

    public synchronized void registrarFalha(
            String enderecoIp,
            String email) {

        String chave = construirChave(enderecoIp, email);
        Instant agora = clock.instant();

        RegistroTentativas registro =
                obterRegistroValido(chave, agora);

        if (registro == null) {
            if (!existeCapacidade(agora)) {
                return;
            }

            registro = new RegistroTentativas(agora);
            registros.put(chave, registro);
        }

        if (registro.bloqueadoAte != null) {
            return;
        }

        registro.falhas++;

        registrarFalhaPorIp(
                normalizarIp(enderecoIp),
                agora
        );

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

        Instant agora = clock.instant();
        String ip = normalizarIp(enderecoIp);

        long segundosPorIp =
                segundosRestantesPorIp(ip, agora);

        String chave = construirChave(enderecoIp, email);

        RegistroTentativas registro =
                obterRegistroValido(chave, agora);

        if (registro == null || registro.bloqueadoAte == null) {
            return segundosPorIp;
        }

        long segundosPorConta = calcularSegundos(
                agora,
                registro.bloqueadoAte
        );

        return Math.max(
                segundosPorConta,
                segundosPorIp
        );
    }

    private boolean permitirPorIp(
            String ip,
            Instant agora) {

        RegistroIp registro = registrosPorIp.get(ip);

        if (registro != null && expirouRegistroIp(registro, agora)) {
            registrosPorIp.remove(ip);
            registro = null;
        }

        if (registro != null) {
            return registro.falhas < MAXIMO_FALHAS_POR_IP;
        }

        if (registrosPorIp.size() >= MAXIMO_REGISTROS) {
            removerRegistrosIpExpirados(agora);

            return registrosPorIp.size() < MAXIMO_REGISTROS;
        }

        return true;
    }

    private void registrarFalhaPorIp(
            String ip,
            Instant agora) {

        RegistroIp registro = registrosPorIp.get(ip);

        if (registro != null && expirouRegistroIp(registro, agora)) {
            registrosPorIp.remove(ip);
            registro = null;
        }

        if (registro == null) {
            if (registrosPorIp.size() >= MAXIMO_REGISTROS) {
                removerRegistrosIpExpirados(agora);

                if (registrosPorIp.size() >= MAXIMO_REGISTROS) {
                    return;
                }
            }

            registro = new RegistroIp(agora);
            registrosPorIp.put(ip, registro);
        }

        if (registro.falhas < MAXIMO_FALHAS_POR_IP) {
            registro.falhas++;
        }
    }

    private long segundosRestantesPorIp(
            String ip,
            Instant agora) {

        RegistroIp registro = registrosPorIp.get(ip);

        if (registro == null) {
            return 0;
        }

        if (expirouRegistroIp(registro, agora)) {
            registrosPorIp.remove(ip);
            return 0;
        }

        if (registro.falhas < MAXIMO_FALHAS_POR_IP) {
            return 0;
        }

        return calcularSegundos(
                agora,
                registro.inicioJanela.plus(JANELA_TENTATIVAS)
        );
    }

    private boolean expirouRegistroIp(
            RegistroIp registro,
            Instant agora) {

        return !agora.isBefore(
                registro.inicioJanela.plus(JANELA_TENTATIVAS)
        );
    }

    private void removerRegistrosIpExpirados(Instant agora) {
        registrosPorIp.entrySet().removeIf(
                entrada -> expirouRegistroIp(
                        entrada.getValue(),
                        agora
                )
        );
    }

    private long calcularSegundos(
            Instant agora,
            Instant termino) {

        if (!agora.isBefore(termino)) {
            return 0;
        }

        long milissegundos = Duration.between(
                agora,
                termino
        ).toMillis();

        return Math.max(
                1L,
                (milissegundos + 999L) / 1000L
        );
    }

    private RegistroTentativas obterRegistroValido(
            String chave,
            Instant agora) {

        RegistroTentativas registro = registros.get(chave);

        if (registro != null && estaExpirado(registro, agora)) {
            registros.remove(chave);
            return null;
        }

        return registro;
    }

    private boolean existeCapacidade(Instant agora) {
        if (registros.size() < MAXIMO_REGISTROS) {
            return true;
        }

        removerRegistrosExpirados(agora);

        return registros.size() < MAXIMO_REGISTROS;
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

        private final Set<UUID> reservasPendentes =
                new HashSet<>();

        private int falhas;
        private Instant bloqueadoAte;

        private RegistroTentativas(Instant inicioJanela) {
            this.inicioJanela = inicioJanela;
        }

        private int totalTentativas() {
            return falhas + reservasPendentes.size();
        }
    }

    private static final class RegistroIp {

        private final Instant inicioJanela;
        private int falhas;

        private RegistroIp(Instant inicioJanela) {
            this.inicioJanela = inicioJanela;
        }
    }
}
