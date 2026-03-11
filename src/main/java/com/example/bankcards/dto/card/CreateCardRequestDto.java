package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class CreateCardRequestDto {
    @NotNull(message = "Срок действия обязателен")
    @Future(message = "Срок действия должен быть в будущем")
    private LocalDate expiryDate;

    @NotNull(message = "id владельца обязателен")
    private UUID ownerId;
}
