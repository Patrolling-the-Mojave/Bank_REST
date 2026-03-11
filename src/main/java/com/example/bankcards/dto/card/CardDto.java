package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о банковской карте")
public class CardDto {
    @Schema(description = "ID карты", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private UUID id;
    @Schema(description = "Замаскированный номер карты", example = "**** **** **** 1234")
    private String maskedNumber;

    @Schema(description = "Имя владельца", example = "testuser")
    private String ownerName;

    @Schema(description = "Срок действия", example = "2030-12-31")
    private LocalDate expiryDate;

    @Schema(description = "Статус карты", example = "ACTIVE", allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"})
    private String status;

    @Schema(description = "Баланс карты", example = "1000.00")
    private BigDecimal balance;
}
