package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenRequestDto {
    @NotBlank(message = "Refresh token обязателен")
    private String refreshToken;
}
