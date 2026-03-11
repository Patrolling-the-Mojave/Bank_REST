package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequestDto;
import com.example.bankcards.service.card.CardAdminService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/admin/cards")
@Tag(name = "Admin - Cards", description = "API для управления картами (только ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminCardController {
    private final CardAdminService cardAdminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать карту", description = "Создание новой банковской карты для пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта создана",
                    content = @Content(
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "403", description = "Нет прав доступа"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public CardDto createCard(@Validated @RequestBody CreateCardRequestDto request) {
        log.info("админ создает карту для пользователя: {}", request.getOwnerId());
        System.out.println("админ создает карту для пользователя");
        return cardAdminService.createCard(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Все карты", description = "Получение списка всех карт с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = Page.class)))
    })
    public Page<CardDto> getAllCards(@Parameter(description = "Номер страницы (от 0)", example = "0")
                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                     @Parameter(description = "Размер страницы", example = "10")
                                     @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("админ запрашивает все карты");
        Pageable pageable = PageRequest.of(from, size);
        return cardAdminService.findAllCards(pageable);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Карты пользователя", description = "Получение всех карт конкретного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public Page<CardDto> getUserCards(@Parameter(description = "ID владельца карт", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
                                      @PathVariable UUID userId,
                                      @Parameter(description = "Номер страницы", example = "0")
                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                      @Parameter(description = "Размер страницы", example = "10")
                                      @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("админ запрашивает карты пользователя: {}", userId);
        Pageable pageable = PageRequest.of(from, size);
        return cardAdminService.findAllCardsByOwnerId(userId, pageable);
    }

    @GetMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Карта по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public CardDto getCardById(@Parameter(description = "ID карты", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
                               @PathVariable UUID cardId) {
        log.info("админ запрашивает карту: {}", cardId);
        return cardAdminService.findById(cardId);
    }

    @PostMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать карту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public CardDto blockCard(@Parameter(description = "ID карты")
                             @PathVariable UUID cardId) {
        log.info("админ блокирует карту: {}", cardId);
        return cardAdminService.blockCard(cardId);
    }

    @PostMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать карту")
    @ApiResponse(responseCode = "200", description = "Успешно")
    public CardDto activateCard(@Parameter(description = "ID карты")
                                @PathVariable UUID cardId) {
        log.info("админ активирует карту: {}", cardId);
        return cardAdminService.activateCard(cardId);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешно"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public void deleteCard(@Parameter(description = "ID карты")
                           @PathVariable UUID cardId) {
        log.info("админ удаляет карту: {}", cardId);
        cardAdminService.removeCard(cardId);
    }
}
