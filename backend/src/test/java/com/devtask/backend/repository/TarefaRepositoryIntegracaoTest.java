
package com.devtask.backend.repository;

import com.devtask.backend.dto.AtualizarTarefaRequest;
import com.devtask.backend.dto.CriarTarefaRequest;
import com.devtask.backend.model.PrioridadeTarefa;
import com.devtask.backend.model.StatusTarefa;
import com.devtask.backend.model.Tarefa;
import com.devtask.backend.model.Usuario;
import com.devtask.backend.service.TarefaService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TarefaRepositoryIntegracaoTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private Usuario usuario1;
    private Usuario usuario2;

    private Tarefa tarefa1;
    private Tarefa tarefa2;
    private Tarefa tarefa3;

    @BeforeEach
    void configurarDados() {
        String identificador = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String senhaHash = passwordEncoder.encode(
                UUID.randomUUID().toString()
        );

        usuario1 = usuarioRepository.saveAndFlush(
                new Usuario(
                        "Usuario Integracao Um",
                        "integracao1-" + identificador + "@example.com",
                        senhaHash
                )
        );

        usuario2 = usuarioRepository.saveAndFlush(
                new Usuario(
                        "Usuario Integracao Dois",
                        "integracao2-" + identificador + "@example.com",
                        senhaHash
                )
        );

        tarefa1 = criarTarefa(
                usuario1.getId(),
                "Tarefa do primeiro usuario",
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.BAIXA
        );

        tarefa2 = criarTarefa(
                usuario1.getId(),
                "Outra tarefa do primeiro usuario",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA
        );

        tarefa3 = criarTarefa(
                usuario2.getId(),
                "Tarefa do segundo usuario",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA
        );

        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveListarSomenteTarefasDoProprietarioNoBanco() {
        autenticar(usuario1);

        List<Tarefa> tarefasUsuario1 = tarefaService.listarTodas();

        assertEquals(2, tarefasUsuario1.size());

        assertTrue(tarefasUsuario1.stream().allMatch(
                tarefa -> usuario1.getId().equals(
                        tarefa.getUsuarioId()
                )
        ));

        autenticar(usuario2);

        List<Tarefa> tarefasUsuario2 = tarefaService.listarTodas();

        assertEquals(1, tarefasUsuario2.size());
        assertEquals(tarefa3.getId(), tarefasUsuario2.get(0).getId());
        assertEquals(
                usuario2.getId(),
                tarefasUsuario2.get(0).getUsuarioId()
        );
    }

    @Test
    void deveFiltrarPorIdEProprietarioNaConsultaJpa() {
        assertTrue(
                tarefaRepository.findByIdAndUsuarioId(
                        tarefa1.getId(),
                        usuario1.getId()
                ).isPresent()
        );

        assertTrue(
                tarefaRepository.findByIdAndUsuarioId(
                        tarefa1.getId(),
                        usuario2.getId()
                ).isEmpty()
        );
    }

    @Test
    void naoDeveConsultarTarefaDeOutroUsuario() {
        autenticar(usuario2);

        assertTrue(
                tarefaService.buscarPorId(tarefa1.getId()).isEmpty()
        );

        assertTrue(
                tarefaService.buscarPorId(tarefa3.getId()).isPresent()
        );
    }

    @Test
    void naoDeveAtualizarTarefaDeOutroUsuario() {
        autenticar(usuario2);

        AtualizarTarefaRequest request =
                new AtualizarTarefaRequest(
                        "Alteracao indevida",
                        "Esta alteracao deve ser bloqueada",
                        StatusTarefa.CONCLUIDA,
                        PrioridadeTarefa.ALTA
                );

        assertTrue(
                tarefaService.atualizar(
                        tarefa1.getId(),
                        request
                ).isEmpty()
        );

        entityManager.flush();
        entityManager.clear();

        Tarefa tarefaPersistida = tarefaRepository
                .findById(tarefa1.getId())
                .orElseThrow();

        assertEquals(
                "Tarefa do primeiro usuario",
                tarefaPersistida.getTitulo()
        );

        assertEquals(
                StatusTarefa.PENDENTE,
                tarefaPersistida.getStatus()
        );

        assertEquals(
                usuario1.getId(),
                tarefaPersistida.getUsuarioId()
        );
    }

    @Test
    void naoDeveExcluirTarefaDeOutroUsuario() {
        autenticar(usuario2);

        boolean excluiu = tarefaService.excluir(tarefa1.getId());

        assertFalse(excluiu);

        entityManager.flush();
        entityManager.clear();

        assertTrue(
                tarefaRepository.existsById(tarefa1.getId())
        );
    }

    @Test
    void deveAplicarFiltrosEPaginacaoSemMisturarUsuarios() {
        autenticar(usuario2);

        Page<Tarefa> resultado = tarefaService.consultar(
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA,
                0,
                10,
                "id",
                Sort.Direction.ASC
        );

        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());

        Tarefa encontrada = resultado.getContent().get(0);

        assertEquals(tarefa3.getId(), encontrada.getId());
        assertEquals(usuario2.getId(), encontrada.getUsuarioId());

        assertNotEquals(tarefa2.getId(), encontrada.getId());
    }

    @Test
    void deveAtribuirProprietarioCorretoAoCadastrarNoBanco() {
        autenticar(usuario2);

        CriarTarefaRequest request = new CriarTarefaRequest(
                "Nova tarefa de integracao",
                "Proprietario definido pelo servidor",
                PrioridadeTarefa.MEDIA
        );

        Tarefa cadastrada = tarefaService.cadastrar(request);

        entityManager.flush();
        entityManager.clear();

        Tarefa persistida = tarefaRepository
                .findById(cadastrada.getId())
                .orElseThrow();

        assertEquals(
                usuario2.getId(),
                persistida.getUsuarioId()
        );

        assertEquals(
                "Nova tarefa de integracao",
                persistida.getTitulo()
        );

        assertEquals(
                StatusTarefa.PENDENTE,
                persistida.getStatus()
        );
    }

    private Tarefa criarTarefa(
            Long usuarioId,
            String titulo,
            StatusTarefa status,
            PrioridadeTarefa prioridade) {

        Tarefa tarefa = new Tarefa(
                null,
                titulo,
                "Registro temporario de integracao",
                status,
                prioridade,
                LocalDateTime.now()
        );

        tarefa.atribuirProprietario(usuarioId);

        return tarefaRepository.saveAndFlush(tarefa);
    }

    private void autenticar(Usuario usuario) {
        UsernamePasswordAuthenticationToken autenticacao =
                new UsernamePasswordAuthenticationToken(
                        usuario.getEmail(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        SecurityContext contexto =
                SecurityContextHolder.createEmptyContext();

        contexto.setAuthentication(autenticacao);

        SecurityContextHolder.setContext(contexto);
    }
}
