package com.example.bankcards.controller.user;

import com.example.bankcards.dto.user.CreateUserRequestDto;
import com.example.bankcards.dto.user.UpdateUserAdminRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.service.user.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "API для управления пользователями (только ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserAdminService userAdminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать пользователя", description = "Создание нового пользователя с назначением ролей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь создан",
                    content = @Content(
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "403", description = "Нет прав доступа"),
            @ApiResponse(responseCode = "409", description = "Username уже существует")
    })
    public UserDto createUser(@Validated @RequestBody CreateUserRequestDto request) {
        log.info("админ создает пользователя: {}", request.getUsername());
        return userAdminService.createUser(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Все пользователи", description = "Получение списка пользователей с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = Page.class)))
    })
    public Page<UserDto> getAllUsers(@Parameter(description = "Номер страницы (от 0)", example = "0")
                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                     @Parameter(description = "Размер страницы", example = "10")
                                     @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("админ запрашивает список пользователей");
        Pageable pageable = PageRequest.of(from, size);
        return userAdminService.findAllUsers(pageable);

    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Пользователь по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    })
    public UserDto getUserById(@Parameter(description = "ID пользователя", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
                               @PathVariable UUID userId) {
        log.info("админ запрашивает пользователя: {}", userId);
        return userAdminService.findUserById(userId);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Поиск по username")
    @ApiResponse(responseCode = "200", description = "Успешно",
            content = @Content(schema = @Schema(implementation = UserDto.class)))
    public UserDto getUserByUsername(@Parameter(description = "Имя пользователя для поиска", required = true, example = "testuser")
                                     @RequestParam String username) {
        log.info("админ ищет пользователя: {}", username);
        return userAdminService.findUserByUsername(username);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public UserDto updateUser(@Validated @RequestBody UpdateUserAdminRequestDto request) {
        return userAdminService.updateUser(request);
    }

    @PostMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public UserDto blockUser(@Parameter(description = "ID пользователя")
                             @PathVariable UUID userId) {
        log.info("админ блокирует пользователя: {}", userId);
        return userAdminService.blockUser(userId);
    }

    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать пользователя")
    @ApiResponse(responseCode = "200", description = "Успешно")
    public UserDto activateUser(@Parameter(description = "ID пользователя")
                                @PathVariable UUID userId) {
        log.info("админ активирует пользователя: {}", userId);
        return userAdminService.activateUser(userId);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public void deleteUser(@Parameter(description = "ID пользователя")
                           @PathVariable UUID userId) {
        userAdminService.deleteUser(userId);
    }
}
