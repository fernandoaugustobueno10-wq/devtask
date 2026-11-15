
package com.devtask.backend.service;

import com.devtask.backend.dto.CadastrarUsuarioRequest;
import com.devtask.backend.exception.EmailJaCadastradoException;
import com.devtask.backend.model.Usuario;
import com.devtask.backend.repository.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;

@Service
public class UsuarioService {

    private static final int TAMANHO_MAXIMO_NOME = 120;
    private static final int TAMANHO_MAXIMO_EMAIL = 254;
    private static final int TAMANHO_MINIMO_SENHA = 12;
    private static final int MAXIMO_BYTES_SENHA = 72;
    private static final String SQLSTATE_VIOLACAO_UNIQUE = "23505";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(CadastrarUsuarioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Dados de cadastro obrigatorios."
            );
        }

        String nome = validarNome(request.nome());
        String email = normalizarEmail(request.email());

        validarSenha(request.senha());

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        String senhaHash = passwordEncoder.encode(request.senha());

        Usuario usuario = new Usuario(
                nome,
                email,
                senhaHash
        );

        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            if (ehViolacaoDeUnicidade(exception)) {
                throw new EmailJaCadastradoException();
            }

            throw exception;
        }
    }

    private boolean ehViolacaoDeUnicidade(Throwable exception) {
        Throwable causa = exception;

        while (causa != null) {
            if (causa instanceof SQLException sqlException) {
                if (SQLSTATE_VIOLACAO_UNIQUE.equals(
                        sqlException.getSQLState())) {
                    return true;
                }
            }

            causa = causa.getCause();
        }

        return false;
    }

    private String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome e obrigatorio."
            );
        }

        String nomeNormalizado = nome.strip();

        if (nomeNormalizado.length() > TAMANHO_MAXIMO_NOME) {
            throw new IllegalArgumentException(
                    "O nome deve ter no maximo 120 caracteres."
            );
        }

        return nomeNormalizado;
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O email e obrigatorio."
            );
        }

        String emailNormalizado = email.strip()
                .toLowerCase(Locale.ROOT);

        if (emailNormalizado.length() > TAMANHO_MAXIMO_EMAIL
                || !emailNormalizado.matches(
                        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
                )) {

            throw new IllegalArgumentException(
                    "Informe um email valido."
            );
        }

        return emailNormalizado;
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException(
                    "A senha e obrigatoria."
            );
        }

        int caracteres = senha.codePointCount(0, senha.length());

        if (caracteres < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException(
                    "A senha deve ter no minimo 12 caracteres."
            );
        }

        int bytes = senha.getBytes(StandardCharsets.UTF_8).length;

        if (bytes > MAXIMO_BYTES_SENHA) {
            throw new IllegalArgumentException(
                    "A senha deve ter no maximo 72 bytes em UTF-8."
            );
        }
    }
}
