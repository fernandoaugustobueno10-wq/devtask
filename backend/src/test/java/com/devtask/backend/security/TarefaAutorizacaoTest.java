
package com.devtask.backend.security;

import com.devtask.backend.config.SecurityConfig;
import com.devtask.backend.controller.TarefaController;
import com.devtask.backend.model.PrioridadeTarefa;
import com.devtask.backend.model.StatusTarefa;
import com.devtask.backend.model.Tarefa;
import com.devtask.backend.model.Usuario;
import com.devtask.backend.repository.TarefaRepository;
import com.devtask.backend.repository.UsuarioRepository;
import com.devtask.backend.service.TarefaService;
import com.devtask.backend.service.UsuarioAutenticadoService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@Import({
        SecurityConfig.class,
        TarefaService.class,
        UsuarioAutenticadoService.class
})
class TarefaAutorizacaoTest {

    private static final String PRIMEIRO_EMAIL =
            "primeiro@example.com";

    private static final String SEGUNDO_EMAIL =
            "segundo@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarefaRepository tarefaRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = PRIMEIRO_EMAIL)
    void devePermitirConsultarPropriaTarefa() throws Exception {
        configurarUsuario(PRIMEIRO_EMAIL, 1L);

        Tarefa tarefa = criarTarefa(1L, 1L);

        when(tarefaRepository.findByIdAndUsuarioId(1L, 1L))
                .thenReturn(Optional.of(tarefa));

        mockMvc.perform(get("/api/tarefas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1));

        verify(tarefaRepository)
                .findByIdAndUsuarioId(1L, 1L);
    }

    @Test
    @WithMockUser(username = SEGUNDO_EMAIL)
    void deveBloquearConsultaDeTarefaDeOutroUsuario()
            throws Exception {

        configurarUsuario(SEGUNDO_EMAIL, 2L);

        mockMvc.perform(get("/api/tarefas/1"))
                .andExpect(status().isNotFound());

        verify(tarefaRepository)
                .findByIdAndUsuarioId(1L, 2L);

        verify(tarefaRepository, never())
                .findById(1L);
    }

    @Test
    @WithMockUser(username = SEGUNDO_EMAIL)
    void deveBloquearEdicaoDeTarefaDeOutroUsuario()
            throws Exception {

        configurarUsuario(SEGUNDO_EMAIL, 2L);

        String dados = """
                {
                    "titulo": "Alteracao nao autorizada",
                    "descricao": "Esta alteracao deve ser bloqueada",
                    "status": "EM_ANDAMENTO",
                    "prioridade": "ALTA"
                }
                """;

        mockMvc.perform(put("/api/tarefas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isNotFound());

        verify(tarefaRepository)
                .findByIdAndUsuarioId(1L, 2L);

        verify(tarefaRepository, never())
                .save(any(Tarefa.class));
    }

    @Test
    @WithMockUser(username = SEGUNDO_EMAIL)
    void deveBloquearExclusaoDeTarefaDeOutroUsuario()
            throws Exception {

        configurarUsuario(SEGUNDO_EMAIL, 2L);

        mockMvc.perform(delete("/api/tarefas/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(tarefaRepository)
                .findByIdAndUsuarioId(1L, 2L);

        verify(tarefaRepository, never())
                .delete(any(Tarefa.class));
    }

    @Test
    @WithMockUser(username = SEGUNDO_EMAIL)
    void deveListarSomenteTarefasDoUsuarioAutenticado()
            throws Exception {

        configurarUsuario(SEGUNDO_EMAIL, 2L);

        Tarefa tarefa = criarTarefa(2L, 2L);

        when(tarefaRepository.findAllByUsuarioId(
                eq(2L),
                any()
        )).thenReturn(List.of(tarefa));

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].usuarioId").value(2))
                .andExpect(jsonPath("$.length()").value(1));

        verify(tarefaRepository)
                .findAllByUsuarioId(eq(2L), any());
    }

    @Test
    @WithMockUser(username = SEGUNDO_EMAIL)
    void deveAtribuirNovaTarefaAoUsuarioAutenticado()
            throws Exception {

        configurarUsuario(SEGUNDO_EMAIL, 2L);

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenAnswer(invocation -> {
                    Tarefa tarefa = invocation.getArgument(0);
                    tarefa.setId(3L);
                    return tarefa;
                });

        String dados = """
                {
                    "titulo": "Tarefa do segundo usuario",
                    "descricao": "Proprietario definido pelo servidor",
                    "prioridade": "MEDIA",
                    "usuarioId": 1
                }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.usuarioId").value(2));

        verify(tarefaRepository).save(any(Tarefa.class));
    }

    private void configurarUsuario(String email, Long id) {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getId()).thenReturn(id);

        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(usuario));
    }

    private Tarefa criarTarefa(Long tarefaId, Long usuarioId) {
        Tarefa tarefa = new Tarefa(
                tarefaId,
                "Tarefa de teste",
                "Verificacao de autorizacao",
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.MEDIA,
                LocalDateTime.now()
        );

        tarefa.atribuirProprietario(usuarioId);

        return tarefa;
    }
}
