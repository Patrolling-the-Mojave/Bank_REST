package com.example.bankcards.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UpdateUserAdminRequestDto {
    @NotNull
    private UUID id;
    @Size(min = 3, max = 50, message = "неверный размер имени пользователя, минимум - 3, максимум - 50 символов")
    private String username;
    @Size(min = 6, message = "минимальная длинна пароля - 6 символов")
    private String password;
    private Boolean isActive;
    private Set<String> roles;
}
