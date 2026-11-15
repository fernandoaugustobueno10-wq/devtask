
package com.devtask.backend.service;

import com.devtask.backend.model.Usuario;
import com.devtask.backend.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException(
                    "Credenciais invalidas."
            );
        }

        String emailNormalizado = email.strip()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByEmail(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Credenciais invalidas."
                ));

        return User.withUsername(usuario.getEmail())
                .password(usuario.senhaHashParaAutenticacao())
                .roles("USER")
                .build();
    }
}
