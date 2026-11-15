
package com.devtask.backend.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
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
                        .successHandler((request, response, authentication) -> {
                            response.setHeader(
                                    "Cache-Control",
                                    "no-store"
                            );

                            response.setStatus(
                                    HttpServletResponse.SC_NO_CONTENT
                            );
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setHeader(
                                    "Cache-Control",
                                    "no-store"
                            );

                            response.setStatus(
                                    HttpServletResponse.SC_UNAUTHORIZED
                            );
                        })
                        .permitAll()
                )
                .logout(withDefaults())
                .csrf(withDefaults());

        return http.build();
    }
}
