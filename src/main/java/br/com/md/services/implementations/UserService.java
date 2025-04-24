package br.com.md.services.implementations;

import br.com.md.domain.User;
import br.com.md.dtos.UserOutDTO;
import br.com.md.dtos.UserRegisterDTO;
import br.com.md.dtos.UserUpdateDTO;
import br.com.md.repositories.UserRepository;
import br.com.md.services.CloudinaryService;
import br.com.md.services.interfaces.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                       CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public boolean registerUser(UserRegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getPasswordConfirmation())) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return false;
        }

        String hashed = passwordEncoder.encode(dto.getPassword());
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(hashed);
        userRepository.save(user);
        return true;
    }

    @Override
    public Page<UserOutDTO> findByActiveAndRole(boolean active, String role, Pageable pageable) {
        return userRepository.findAllByActiveAndRole(active, role, pageable)
                .map(this::toOutDTO);
    }

    @Override
    public List<UserOutDTO> searchByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email)
                .stream()
                .map(this::toOutDTO)
                .toList();
    }


    @Override
    public void updateUser(Long id, UserUpdateDTO dto, String authenticatedEmail) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        User user = optionalUser.get();

        if (!user.getEmail().equals(authenticatedEmail)) {
            throw new SecurityException("Você não pode editar outro usuário.");
        }

        user.setName(dto.getName());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (!dto.getPassword().equals(dto.getPasswordConfirmation())) {
                throw new IllegalArgumentException("A confirmação da senha não corresponde.");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
    }


    @Override
    public void toggleUserStatus(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActive(!user.isActive());
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
    }

    @Override
    public void updateProfilePhoto(Long id, MultipartFile file, String authenticatedEmail) throws IOException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        User user = optionalUser.get();

        if (!user.getEmail().equals(authenticatedEmail)) {
            throw new SecurityException("Você não pode alterar a foto de outro usuário.");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("O arquivo enviado não é uma imagem.");
        }

        if (user.getProfileImagePublicId() != null) {
            cloudinaryService.deleteImage(user.getProfileImagePublicId());
        }

        Map<String, String> uploadResult = cloudinaryService.uploadImageWithPublicId(file, id);

        user.setProfileImageUrl(uploadResult.get("url"));
        user.setProfileImagePublicId(uploadResult.get("public_id"));

        userRepository.save(user);
    }

    private UserOutDTO toOutDTO(User user) {
        UserOutDTO dto = new UserOutDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        return dto;
    }
}
