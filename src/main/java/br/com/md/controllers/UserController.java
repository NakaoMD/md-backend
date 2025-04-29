package br.com.md.controllers;

import br.com.md.dtos.UserOutDTO;
import br.com.md.dtos.UserRegisterDTO;
import br.com.md.dtos.UserUpdateDTO;
import br.com.md.services.interfaces.IUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegisterDTO dto) {
        try {
            boolean registered = userService.registerUser(dto);
            if (registered) {
                return ResponseEntity.ok("Usuário registrado com sucesso!");
            } else {
                return ResponseEntity.status(409).body("E-mail já está em uso.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserOutDTO> getAuthenticatedUser(
            @AuthenticationPrincipal UserDetails loggedUser
    ) {
        UserOutDTO userData = userService.getAuthenticatedUser(loggedUser.getUsername());
        return ResponseEntity.ok(userData);
    }

    @GetMapping
    public ResponseEntity<Page<UserOutDTO>> getUsers(
            @RequestParam boolean active,
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserOutDTO> users = userService.findByActiveAndRole(active, role, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserOutDTO>> searchUsersByEmail(@RequestParam String email) {
        List<UserOutDTO> users = userService.searchByEmail(email);
        return ResponseEntity.ok(users);
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto,
            @AuthenticationPrincipal UserDetails loggedUser
    ) {
        userService.updateUser(id, dto, loggedUser.getUsername());
        return ResponseEntity.ok("Usuário atualizado com sucesso.");
    }


    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<String> toggleStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok("Status do usuário atualizado.");
    }

    @PutMapping(path = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatePhoto(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails loggedUser
    ) throws IOException {
        userService.updateProfilePhoto(id, file, loggedUser.getUsername());
        return ResponseEntity.ok("Foto de perfil atualizada com sucesso.");
    }
}
