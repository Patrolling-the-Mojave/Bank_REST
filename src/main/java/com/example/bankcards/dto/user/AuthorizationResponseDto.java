package com.example.bankcards.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorizationResponseDto {
    private String token;
    private String refreshToken;
    private UserDto user;
}
