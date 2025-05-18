package br.com.md.services.implementations;

import br.com.md.domain.User;
import br.com.md.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Autenticação padrão (e-mail + senha)
    public Optional<User> authenticate(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            if (password == null) {
                // Autenticação OAuth2: usuário já registrado
                return user;
            }

            if (passwordEncoder.matches(password, user.get().getPassword())) {
                return user;
            }
        }
        return Optional.empty();
    }

    // Registro de novo usuário via Google OAuth
    public User registerOAuthUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword("oauth"); // marcador para logins sociais
        user.setActive(true);
        user.setRole("USER");
        return userRepository.save(user);
    }
}
