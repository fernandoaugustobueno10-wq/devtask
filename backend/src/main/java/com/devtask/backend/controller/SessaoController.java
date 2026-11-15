
package com.devtask.backend.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SessaoController {

    @GetMapping("/sessao")
    public ResponseEntity<SessaoResponse> verificarSessao() {
        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        boolean autenticado =
                autenticacao != null
                && autenticacao.isAuthenticated()
                && !(autenticacao instanceof AnonymousAuthenticationToken);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new SessaoResponse(autenticado));
    }

    public record SessaoResponse(boolean autenticado) {
    }
}
