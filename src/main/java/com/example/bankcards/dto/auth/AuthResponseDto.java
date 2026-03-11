package com.example.bankcards.dto.auth;

import com.example.bankcards.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String token;
    private String refreshToken;
    private UserDto user;
}
