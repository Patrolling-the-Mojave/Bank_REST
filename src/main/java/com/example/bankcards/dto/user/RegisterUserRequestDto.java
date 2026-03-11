package com.example.bankcards.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserRequestDto {
    @NotBlank
    @Size(min = 3, max = 50, message = "неверный размер имени пользователя, минимум - 3, максимум - 50 символов")
    private String username;
    @NotBlank
    @Size(min = 6, message = "минимальная длинна пароля - 6 символов")
    private String password;
}
