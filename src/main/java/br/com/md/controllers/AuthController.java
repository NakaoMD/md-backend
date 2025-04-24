package br.com.md.controllers;

import br.com.md.config.JwtUtil;
import br.com.md.domain.User;
import br.com.md.dtos.UserLoginDTO;
import br.com.md.services.implementations.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO dto) {
        Optional<User> user = authService.authenticate(dto.getEmail(), dto.getPassword());
        if (user.isPresent()) {
            String token = jwtUtil.generateToken(dto.getEmail());
            return ResponseEntity.ok(Map.of("token", "Bearer " + token));
        }
        return ResponseEntity.status(401).body("Usuário ou senha inválidos.");
    }
}
