
package com.devtask.backend.config;

import com.devtask.backend.service.LimitadorTentativasLogin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class FiltroLimiteLogin extends OncePerRequestFilter {

    public static final String ATRIBUTO_RESERVA =
            FiltroLimiteLogin.class.getName() + ".reserva";

    private static final String CAMINHO_LOGIN =
            "/api/auth/login";

    private static final String METODO_LOGIN = "POST";

    private static final long ESPERA_CAPACIDADE_SEGUNDOS = 1L;

    private final LimitadorTentativasLogin limitador;

    public FiltroLimiteLogin(
            LimitadorTentativasLogin limitador) {

        this.limitador = limitador;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        if (!METODO_LOGIN.equalsIgnoreCase(
                request.getMethod())) {

            return true;
        }

        String contexto = request.getContextPath();
        String uri = request.getRequestURI();

        if (uri == null || contexto == null) {
            return true;
        }

        if (!uri.startsWith(contexto)) {
            return true;
        }

        String caminho = uri.substring(contexto.length());

        return !CAMINHO_LOGIN.equals(caminho);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String enderecoIp = request.getRemoteAddr();
        String email = request.getParameter("username");

        UUID identificador =
                limitador.reservarTentativaIdentificada(
                        enderecoIp,
                        email
                );

        if (identificador == null) {
            long segundosRestantes =
                    limitador.segundosRestantes(
                            enderecoIp,
                            email
                    );

            if (segundosRestantes <= 0) {
                segundosRestantes =
                        ESPERA_CAPACIDADE_SEGUNDOS;
            }

            response.setHeader(
                    "Cache-Control",
                    "no-store"
            );

            response.setHeader(
                    "Retry-After",
                    Long.toString(segundosRestantes)
            );

            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            return;
        }

        request.setAttribute(
                ATRIBUTO_RESERVA,
                identificador
        );

        filterChain.doFilter(request, response);
    }
}
