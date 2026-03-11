package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.BlockCardRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.service.card.CardPrivateService;
import com.example.bankcards.service.user.UserPrivateService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "User - Cards", description = "API для управления своими картами")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/private/cards")
public class PrivateCardController {
    private final CardPrivateService cardPrivateService;
    private final UserPrivateService userPrivateService;

    @GetMapping
    @Operation(summary = "Мои карты", description = "Получение списка своих карт с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public Page<CardDto> finAllMyCards(@Parameter(description = "Номер страницы (от 0)", example = "0")
                                       @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                       @Parameter(description = "Размер страницы", example = "10")
                                       @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        UUID currentUserId = getCurrentUserId();
        Pageable pageable = PageRequest.of(from, size);
        return cardPrivateService.findAllCardsByOwnerId(currentUserId, pageable);
    }

    @GetMapping("/{cardId}/balance")
    @Operation(summary = "Баланс карты", description = "Получение баланса конкретной карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к карте"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public BigDecimal getCardBalance(@Parameter(description = "ID карты", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
                                     @PathVariable UUID cardId) {
        UUID userId = getCurrentUserId();
        return cardPrivateService.getBalanceByCardId(cardId, userId);
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Карта по ID", description = "Получение информации о своей карте")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к карте"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public CardDto findCardById(@Parameter(description = "ID карты", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
                                @PathVariable UUID cardId) {
        UUID ownerId = getCurrentUserId();
        return cardPrivateService.findById(cardId, ownerId);
    }

    @PostMapping("/block")
    @Operation(summary = "Заблокировать карту", description = "Запрос на блокировку своей карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к карте")
    })
    public CardDto blockCard(@Validated @RequestBody BlockCardRequest blockRequest) {
        return cardPrivateService.blockCard(blockRequest);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод между картами", description = "Перевод денег между своими картами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств / Ошибка валидации"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к карте")
    })
    public void transfer(@Validated @RequestBody TransferRequest request) {
        UUID userId = getCurrentUserId();
        cardPrivateService.transfer(request, userId);
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userPrivateService.getCurrentUserIdByUsername(username);
    }
}
