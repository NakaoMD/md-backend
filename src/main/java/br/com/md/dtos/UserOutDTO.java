package br.com.md.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserOutDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean active;
    private String profileImageUrl;
}
