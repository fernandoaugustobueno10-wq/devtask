
package com.devtask.backend.controller;

import com.devtask.backend.dto.AtualizarTarefaRequest;
import com.devtask.backend.dto.CriarTarefaRequest;
import com.devtask.backend.model.PrioridadeTarefa;
import com.devtask.backend.model.StatusTarefa;
import com.devtask.backend.model.Tarefa;
import com.devtask.backend.service.TarefaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
@Tag(
        name = "Tarefas",
        description = "Operacoes de gerenciamento de tarefas"
)
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @Operation(
            summary = "Listar todas as tarefas",
            description = "Retorna todas as tarefas cadastradas, ordenadas pelo ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de tarefas retornada com sucesso"
    )
    @GetMapping
    public List<Tarefa> listarTarefas() {
        return tarefaService.listarTodas();
    }

    @Operation(
            summary = "Consultar tarefas com filtros e paginacao",
            description = "Permite filtrar tarefas por status e prioridade, "
                    + "definir a pagina, o tamanho e a ordenacao dos resultados. "
                    + "O tamanho maximo de cada pagina e 100."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Consulta realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametros de consulta invalidos"
            )
    })
    @GetMapping("/consulta")
    public Page<Tarefa> consultarTarefas(
            @Parameter(description = "Status da tarefa")
            @RequestParam(required = false)
            StatusTarefa status,

            @Parameter(description = "Prioridade da tarefa")
            @RequestParam(required = false)
            PrioridadeTarefa prioridade,

            @Parameter(description = "Numero da pagina, iniciando em zero")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Quantidade por pagina, entre 1 e 100")
            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(
                    description = "Campo: id, titulo, status, prioridade ou dataCriacao"
            )
            @RequestParam(defaultValue = "id")
            String ordenarPor,

            @Parameter(description = "Direcao: ASC ou DESC")
            @RequestParam(defaultValue = "ASC")
            Sort.Direction direcao) {

        return tarefaService.consultar(
                status,
                prioridade,
                page,
                size,
                ordenarPor,
                direcao
        );
    }

    @Operation(
            summary = "Buscar tarefa por ID",
            description = "Retorna uma tarefa especifica pelo seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarefa encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador com formato invalido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa nao encontrada"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarTarefaPorId(
            @Parameter(description = "Identificador da tarefa", required = true)
            @PathVariable Long id) {

        return tarefaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Cadastrar tarefa",
            description = "Cadastra uma nova tarefa com status inicial PENDENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tarefa cadastrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da tarefa invalidos"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tarefa cadastrarTarefa(
            @Valid @RequestBody CriarTarefaRequest request) {

        return tarefaService.cadastrar(request);
    }

    @Operation(
            summary = "Atualizar tarefa",
            description = "Atualiza titulo, descricao, status e prioridade "
                    + "de uma tarefa existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarefa atualizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados ou identificador invalidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa nao encontrada"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizarTarefa(
            @Parameter(description = "Identificador da tarefa", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AtualizarTarefaRequest request) {

        return tarefaService.atualizar(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Excluir tarefa",
            description = "Exclui permanentemente uma tarefa pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Tarefa excluida com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador invalido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa nao encontrada"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirTarefa(
            @Parameter(description = "Identificador da tarefa", required = true)
            @PathVariable Long id) {

        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        if (tarefaService.excluir(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
