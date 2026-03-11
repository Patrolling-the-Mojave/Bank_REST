package com.example.bankcards.controller.user;

import com.example.bankcards.dto.user.UpdateUserClientRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.service.user.UserPrivateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "User - Profile", description = "API для управления своим профилем")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/private/users")
public class PrivateUserController {
    private final UserPrivateService userPrivateService;

    @GetMapping
    @Operation(summary = "Мой профиль", description = "Получение информации о текущем пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public UserDto getMyProfile() {
        UUID userId = getCurrentUserId();
        log.info("пользователь {} запрашивает свой профиль", userId);
        return userPrivateService.getMyProfileById(userId);
    }

    @PutMapping
    @Operation(summary = "Обновить профиль", description = "Обновление данных своего профиля")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public UserDto updateMyProfile(@Validated @RequestBody UpdateUserClientRequestDto request) {
        UUID userId = getCurrentUserId();
        log.info("пользователь {} обновляет профиль", userId);
        return userPrivateService.updateProfile(request);
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userPrivateService.getCurrentUserIdByUsername(username);
    }
}
