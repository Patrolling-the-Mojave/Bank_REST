package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Запрос на перевод между картами")
public class TransferRequest {

    @Schema(description = "ID карты отправителя", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    @NotNull(message = "ID карты отправителя обязателен")
    private UUID fromCardId;

    @Schema(description = "ID карты получателя", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12")
    @NotNull(message = "ID карты получателя обязателен")
    private UUID toCardId;

    @Schema(description = "Сумма перевода", example = "100.00")
    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Минимальная сумма 0.01")
    private BigDecimal amount;
}
