package com.example.bankcards.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Запрос на аутентификацию")
public class LoginRequestDto {

    @Schema(description = "Имя пользователя", example = "testuser")
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @Schema(description = "Пароль", example = "password123")
    @NotBlank(message = "Пароль обязателен")
    private String password;
}