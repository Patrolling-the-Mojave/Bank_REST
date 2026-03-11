package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CardAdminService {
    CardDto createCard(CreateCardRequestDto newCard);

    CardDto blockCard(UUID cardId);

    CardDto activateCard(UUID cardId);

    void removeCard(UUID cardId);

    Page<CardDto> findAllCardsByOwnerId(UUID ownerId, Pageable pageable);

    Page<CardDto> findAllCards(Pageable pageable);

    CardDto findById(UUID cardId);
}
