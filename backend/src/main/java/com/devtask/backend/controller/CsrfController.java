
package com.devtask.backend.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class CsrfController {

    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> obterToken(
            @RequestAttribute("_csrf") CsrfToken csrfToken) {

        CsrfResponse resposta = new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getToken()
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(resposta);
    }

    public record CsrfResponse(
            String headerName,
            String token
    ) {
    }
}
