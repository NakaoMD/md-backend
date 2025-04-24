package br.com.md.services.interfaces;

import br.com.md.dtos.UserOutDTO;
import br.com.md.dtos.UserRegisterDTO;
import br.com.md.dtos.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IUserService {
    boolean registerUser(UserRegisterDTO dto);

    Page<UserOutDTO> findByActiveAndRole(boolean active, String role, Pageable pageable);

    List<UserOutDTO> searchByEmail(String email);

    void updateUser(Long id, UserUpdateDTO dto, String authenticatedEmail);

    void toggleUserStatus(Long id);

    void updateProfilePhoto(Long id, MultipartFile file, String authenticatedEmail) throws IOException;
}
