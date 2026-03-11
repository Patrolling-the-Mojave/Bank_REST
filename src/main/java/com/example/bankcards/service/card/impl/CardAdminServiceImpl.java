package com.example.bankcards.service.card.impl;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequestDto;
import com.example.bankcards.service.card.CardAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardAdminServiceImpl implements CardAdminService {
    private final CardOperationService cardOperationService;

    @Override
    public CardDto activateCard(UUID cardId) {
        log.debug("запрос на активацию карты с id{}", cardId);
        return cardOperationService.activateCard(cardId);
    }

    @Override
    public CardDto createCard(CreateCardRequestDto requestDto) {
        log.debug("запрос на создание карты для пользователя{}", requestDto.getOwnerId());
        return cardOperationService.createCard(requestDto);
    }

    @Override
    public CardDto blockCard(UUID cardId) {
        log.debug("запрос на блокировку карты с id{}", cardId);
        return cardOperationService.blockCard(cardId);
    }

    @Override
    public void removeCard(UUID cardId) {
        log.debug("запрос на удаление карты с id{}", cardId);
        cardOperationService.deleteCard(cardId);
    }

    @Override
    public Page<CardDto> findAllCardsByOwnerId(UUID ownerId, Pageable pageable) {
        log.debug("поиск всех карт пользователя{}", ownerId);
        return cardOperationService.findAllCardsByOwnerId(ownerId, pageable);
    }

    @Override
    public Page<CardDto> findAllCards(Pageable pageable) {
        log.debug("поиск всех карт");
        return cardOperationService.findAllCards(pageable);
    }

    @Override
    public CardDto findById(UUID cardId) {
        return cardOperationService.findById(cardId);
    }
}
