package com.example.bankcards.controller.user;

import com.example.bankcards.dto.auth.AuthResponseDto;
import com.example.bankcards.dto.auth.LoginRequestDto;
import com.example.bankcards.dto.auth.RefreshTokenRequestDto;
import com.example.bankcards.dto.user.RegisterUserRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.user.UserPrivateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации и регистрации")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserPrivateService userPrivateService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Создаёт нового пользователя с ролью ROLE_USER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная регистрация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует")
    })
    public AuthResponseDto register(@Validated @RequestBody RegisterUserRequestDto registerRequest) {
        log.debug("регистрация пользователя{}", registerRequest.getUsername());
        UserDto userDto = userPrivateService.register(registerRequest);

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                registerRequest.getUsername(),
                registerRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        AuthResponseDto response = AuthResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
        return response;
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Возвращает JWT токены при успешной аутентификации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(
                            schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учётные данные"),
            @ApiResponse(responseCode = "403", description = "Пользователь заблокирован")
    })
    public AuthResponseDto login(@Validated @RequestBody LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        UserDto userDTO = userPrivateService.getMyProfileById(getCurrentUserId(authentication)
        );

        AuthResponseDto response = AuthResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();

        return response;
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление токена", description = "Получение новых access и refresh токенов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токены обновлены",
                    content = @Content(
                            schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный refresh token")
    })
    public ResponseEntity<AuthResponseDto> refresh(@Validated @RequestBody RefreshTokenRequestDto request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().build();
        }
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, null)
        );
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        UserDto userDTO = userPrivateService.getMyProfileById(
                getCurrentUserId(authentication)
        );

        AuthResponseDto response = AuthResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userDTO)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-username")
    @Operation(summary = "Проверка существования username", description = "Возвращает true, если username уже занят")
    @ApiResponse(responseCode = "200", description = "Успешная проверка")
    public Boolean checkUsername(@Parameter(description = "Имя пользователя для проверки", required = true, example = "testuser")
                                 @RequestParam String username) {
        return userPrivateService.checkUsernameExists(username);
    }

    private UUID getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        return userPrivateService.getCurrentUserIdByUsername(username);
    }
}
