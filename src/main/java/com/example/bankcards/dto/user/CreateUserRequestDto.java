package com.example.bankcards.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CreateUserRequestDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, message = "пароль должен содержать минимум 6 символов")
    private String password;

    @NotEmpty(message = "хотя бы одна роль обязательна")
    private Set<String> roles;
}
