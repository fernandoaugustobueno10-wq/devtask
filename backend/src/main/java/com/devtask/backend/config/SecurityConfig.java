
package com.devtask.backend.config;

import com.devtask.backend.service.LimitadorTentativasLogin;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public LimitadorTentativasLogin limitadorTentativasLogin() {
        return new LimitadorTentativasLogin();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LimitadorTentativasLogin limitador)
            throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/csrf",
                                "/api/auth/sessao"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/cadastro"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(
                                (request, response, authentication) -> {
                                    limitador.registrarSucesso(
                                            request.getRemoteAddr(),
                                            request.getParameter("username")
                                    );

                                    response.setHeader(
                                            "Cache-Control",
                                            "no-store"
                                    );

                                    response.setStatus(
                                            HttpServletResponse.SC_NO_CONTENT
                                    );
                                }
                        )
                        .failureHandler(
                                (request, response, exception) -> {
                                    String enderecoIp =
                                            request.getRemoteAddr();

                                    String email =
                                            request.getParameter("username");

                                    response.setHeader(
                                            "Cache-Control",
                                            "no-store"
                                    );

                                    long segundosRestantes =
                                            limitador.segundosRestantes(
                                                    enderecoIp,
                                                    email
                                            );

                                    if (segundosRestantes > 0) {
                                        response.setHeader(
                                                "Retry-After",
                                                Long.toString(
                                                        segundosRestantes
                                                )
                                        );

                                        response.setStatus(
                                                HttpStatus.TOO_MANY_REQUESTS
                                                        .value()
                                        );

                                        return;
                                    }

                                    response.setStatus(
                                            HttpServletResponse.SC_UNAUTHORIZED
                                    );
                                }
                        )
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(
                                (request, response, authentication) -> {
                                    response.setHeader(
                                            "Cache-Control",
                                            "no-store"
                                    );

                                    response.setStatus(
                                            HttpServletResponse.SC_NO_CONTENT
                                    );
                                }
                        )
                        .permitAll()
                )
                .addFilterBefore(
                        new FiltroLimiteLogin(limitador),
                        UsernamePasswordAuthenticationFilter.class
                )
                .csrf(withDefaults());

        return http.build();
    }
}
