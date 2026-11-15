
package com.devtask.backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void configurar() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void deveRetornarBadRequestParaTituloInvalido() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError erro = new FieldError(
                "tarefa",
                "titulo",
                "não deve estar em branco"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(erro));

        ResponseEntity<GlobalExceptionHandler.RespostaErro> resposta =
                handler.tratarValidacao(exception);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(400, resposta.getBody().status());
        assertEquals("Dados invalidos", resposta.getBody().erro());
        assertEquals(
                "não deve estar em branco",
                resposta.getBody().campos().get("titulo")
        );
        assertNotNull(resposta.getBody().timestamp());
    }

    @Test
    void deveInformarTodosOsCamposInvalidos() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError erroTitulo = new FieldError(
                "tarefa",
                "titulo",
                "não deve estar em branco"
        );

        FieldError erroPrioridade = new FieldError(
                "tarefa",
                "prioridade",
                "não deve ser nulo"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(erroTitulo, erroPrioridade));

        ResponseEntity<GlobalExceptionHandler.RespostaErro> resposta =
                handler.tratarValidacao(exception);

        assertNotNull(resposta.getBody());

        Map<String, String> campos = resposta.getBody().campos();

        assertEquals(2, campos.size());
        assertEquals(
                "não deve estar em branco",
                campos.get("titulo")
        );
        assertEquals(
                "não deve ser nulo",
                campos.get("prioridade")
        );
    }

    @Test
    void deveRetornarBadRequestParaJsonInvalido() {
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<GlobalExceptionHandler.RespostaErro> resposta =
                handler.tratarCorpoInvalido(exception);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(400, resposta.getBody().status());
        assertEquals("Requisicao invalida", resposta.getBody().erro());
        assertEquals(
                "O corpo da requisicao esta ausente ou contem dados invalidos.",
                resposta.getBody().mensagem()
        );
        assertTrue(resposta.getBody().campos().isEmpty());
    }

    @Test
    void naoDeveExporDetalhesInternosDaExcecao() {
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        when(exception.getMessage())
                .thenReturn("Erro interno com dados sensiveis");

        ResponseEntity<GlobalExceptionHandler.RespostaErro> resposta =
                handler.tratarCorpoInvalido(exception);

        assertNotNull(resposta.getBody());

        String mensagem = resposta.getBody().mensagem();

        assertFalse(mensagem.contains("dados sensiveis"));
        assertFalse(mensagem.contains("Erro interno"));
        assertTrue(resposta.getBody().campos().isEmpty());
    }
}
