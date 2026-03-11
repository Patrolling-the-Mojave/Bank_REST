package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BlockCardRequest {
    @NotNull
    private UUID cardId;
    @NotBlank(message = "причина блокировки обязательна")
    private String reason;
    @NotNull
    private UUID ownerId;
}
