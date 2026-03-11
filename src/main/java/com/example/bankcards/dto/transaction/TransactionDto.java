package com.example.bankcards.dto.transaction;

import com.example.bankcards.entity.Card;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionDto {
    private UUID id;
    private UUID fromCard;
    private UUID toCard;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
