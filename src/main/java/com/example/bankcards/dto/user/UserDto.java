package com.example.bankcards.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserDto {
    @Schema(description = "ID пользователя", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private UUID id;
    @Schema(description = "Имя пользователя", example = "testuser")
    private String username;

    @Schema(description = "Статус активности", example = "true")
    private Boolean isActive;

    @Schema(description = "Роли пользователя", example = "ROLE_USER")
    private Set<String> roles;
}
