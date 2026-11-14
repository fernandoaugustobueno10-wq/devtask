
package com.devtask.backend.dto;

import com.devtask.backend.model.PrioridadeTarefa;
import com.devtask.backend.model.StatusTarefa;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TarefaRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void configurar() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void finalizar() {
        validatorFactory.close();
    }

    private boolean possuiErroNaPropriedade(
            Set<? extends ConstraintViolation<?>> violacoes,
            String propriedade) {

        return violacoes.stream()
                .anyMatch(violacao ->
                        propriedade.equals(
                                violacao.getPropertyPath().toString()
                        )
                );
    }

    @Test
    void deveAceitarCadastroValido() {
        CriarTarefaRequest request = new CriarTarefaRequest(
                "Estudar Java",
                "Revisar Spring Boot",
                PrioridadeTarefa.ALTA
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void naoDeveAceitarTituloVazioNoCadastro() {
        CriarTarefaRequest request = new CriarTarefaRequest(
                "   ",
                "Descricao valida",
                PrioridadeTarefa.MEDIA
        );

        assertTrue(possuiErroNaPropriedade(
                validator.validate(request),
                "titulo"
        ));
    }

    @Test
    void naoDeveAceitarTituloMuitoLongo() {
        CriarTarefaRequest request = new CriarTarefaRequest(
                "A".repeat(121),
                "Descricao valida",
                PrioridadeTarefa.ALTA
        );

        assertTrue(possuiErroNaPropriedade(
                validator.validate(request),
                "titulo"
        ));
    }

    @Test
    void naoDeveAceitarDescricaoMuitoLonga() {
        CriarTarefaRequest request = new CriarTarefaRequest(
                "Tarefa valida",
                "A".repeat(2001),
                PrioridadeTarefa.BAIXA
        );

        assertTrue(possuiErroNaPropriedade(
                validator.validate(request),
                "descricao"
        ));
    }

    @Test
    void naoDeveAceitarPrioridadeNula() {
        CriarTarefaRequest request = new CriarTarefaRequest(
                "Tarefa valida",
                "Descricao valida",
                null
        );

        assertTrue(possuiErroNaPropriedade(
                validator.validate(request),
                "prioridade"
        ));
    }

    @Test
    void deveAceitarAtualizacaoValida() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest(
                "Atualizar tarefa",
                "Descricao atualizada",
                StatusTarefa.CONCLUIDA,
                PrioridadeTarefa.ALTA
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void naoDeveAceitarStatusNuloNaAtualizacao() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest(
                "Atualizar tarefa",
                "Descricao atualizada",
                null,
                PrioridadeTarefa.MEDIA
        );

        assertTrue(possuiErroNaPropriedade(
                validator.validate(request),
                "status"
        ));
    }

    @Test
    void naoDeveAceitarTituloVazioNaAtualizacao() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest(
                "",
                "Descricao atualizada",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA
        );

        assertTrue(possuiErroNaPropriedade(
                validator.validate(request),
                "titulo"
        ));
    }
}
