
package com.devtask.backend.service;

import com.devtask.backend.dto.CadastrarUsuarioRequest;
import com.devtask.backend.exception.EmailJaCadastradoException;
import com.devtask.backend.model.Usuario;
import com.devtask.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService usuarioService;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void configurar() {
        passwordEncoder = new BCryptPasswordEncoder(4);

        usuarioService = new UsuarioService(
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void deveCadastrarUsuarioComSenhaProtegida() {
        CadastrarUsuarioRequest request = criarRequestValido();

        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals("Fernando", resultado.getNome());
        assertEquals("fernando@exemplo.com", resultado.getEmail());

        String hash = (String) ReflectionTestUtils.getField(
                resultado,
                "senhaHash"
        );

        assertNotNull(hash);
        assertNotEquals("MinhaSenhaSegura123", hash);
        assertTrue(passwordEncoder.matches(
                "MinhaSenhaSegura123",
                hash
        ));

        verify(usuarioRepository).saveAndFlush(any(Usuario.class));
    }

    @Test
    void deveNormalizarNomeEEmail() {
        CadastrarUsuarioRequest request =
                new CadastrarUsuarioRequest(
                        "  Fernando  ",
                        "  FERNANDO@EXEMPLO.COM  ",
                        "MinhaSenhaSegura123"
                );

        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.cadastrar(request);

        assertEquals("Fernando", resultado.getNome());
        assertEquals("fernando@exemplo.com", resultado.getEmail());

        verify(usuarioRepository).existsByEmail(
                "fernando@exemplo.com"
        );

        verify(usuarioRepository).saveAndFlush(any(Usuario.class));
    }

    @Test
    void naoDeveCadastrarEmailDuplicado() {
        CadastrarUsuarioRequest request = criarRequestValido();

        when(usuarioRepository.existsByEmail(
                "fernando@exemplo.com"
        )).thenReturn(true);

        assertThrows(
                EmailJaCadastradoException.class,
                () -> usuarioService.cadastrar(request)
        );

        verify(usuarioRepository, never())
                .saveAndFlush(any(Usuario.class));
    }

    @Test
    void naoDeveCadastrarSenhaCurta() {
        CadastrarUsuarioRequest request =
                new CadastrarUsuarioRequest(
                        "Fernando",
                        "fernando@exemplo.com",
                        "senha123"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.cadastrar(request)
        );

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void naoDeveCadastrarSenhaAcimaDe72Bytes() {
        CadastrarUsuarioRequest request =
                new CadastrarUsuarioRequest(
                        "Fernando",
                        "fernando@exemplo.com",
                        "á".repeat(37)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.cadastrar(request)
        );

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void naoDeveCadastrarEmailInvalido() {
        CadastrarUsuarioRequest request =
                new CadastrarUsuarioRequest(
                        "Fernando",
                        "email-invalido",
                        "MinhaSenhaSegura123"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.cadastrar(request)
        );

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void naoDeveCadastrarNomeVazio() {
        CadastrarUsuarioRequest request =
                new CadastrarUsuarioRequest(
                        "   ",
                        "fernando@exemplo.com",
                        "MinhaSenhaSegura123"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.cadastrar(request)
        );

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveTratarViolacaoUniqueComoEmailDuplicado() {
        CadastrarUsuarioRequest request = criarRequestValido();

        SQLException causa = new SQLException(
                "Violacao de unicidade",
                "23505"
        );

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Violacao de integridade",
                        causa
                );

        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenThrow(exception);

        assertThrows(
                EmailJaCadastradoException.class,
                () -> usuarioService.cadastrar(request)
        );

        verify(usuarioRepository).saveAndFlush(any(Usuario.class));
    }

    @Test
    void naoDeveConfundirOutrasViolacoesComEmailDuplicado() {
        CadastrarUsuarioRequest request = criarRequestValido();

        SQLException causa = new SQLException(
                "Violacao de check",
                "23514"
        );

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Violacao de integridade",
                        causa
                );

        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenThrow(exception);

        DataIntegrityViolationException resultado = assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioService.cadastrar(request)
        );

        assertSame(exception, resultado);

        verify(usuarioRepository).saveAndFlush(any(Usuario.class));
    }

    private CadastrarUsuarioRequest criarRequestValido() {
        return new CadastrarUsuarioRequest(
                "Fernando",
                "fernando@exemplo.com",
                "MinhaSenhaSegura123"
        );
    }
}
