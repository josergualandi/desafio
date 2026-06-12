package br.com.desafio.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    @Test
    void deveCriarPasswordEncoderEUsuarioPadrao() {
        SecurityConfig config = new SecurityConfig();

        PasswordEncoder encoder = config.passwordEncoder();
        UserDetailsService userDetailsService = config.userDetailsService(encoder);
        UserDetails user = userDetailsService.loadUserByUsername("admin");

        assertTrue(encoder.matches("admin123", user.getPassword()));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }
}
