package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.BlockCardRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.TransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardPrivateService {

    Page<CardDto> findAllCardsByOwnerId(UUID userId, Pageable pageable);

    CardDto blockCard(BlockCardRequest request);

    void transfer(TransferRequest request, UUID userId);

    CardDto findById(UUID cardId, UUID userId);

    BigDecimal getBalanceByCardId(UUID cardId, UUID userId);
}
