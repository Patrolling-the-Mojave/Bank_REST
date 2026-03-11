package com.example.bankcards.service.card.impl;

import com.example.bankcards.dto.card.BlockCardRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardAccessException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.card.CardPrivateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardPrivateServiceImpl implements CardPrivateService {
    private final CardOperationService cardOperationService;
    private final CardRepository cardRepository;

    @Override
    public CardDto blockCard(BlockCardRequest request) {
        validateCardOwner(request.getCardId(), request.getOwnerId());
        return cardOperationService.blockCard(request.getCardId());
    }

    @Override
    public Page<CardDto> findAllCardsByOwnerId(UUID userId, Pageable pageable) {
        return cardOperationService.findAllCardsByOwnerId(userId, pageable);
    }

    @Override
    public void transfer(TransferRequest request, UUID userId) {
        validateCardOwner(request.getFromCardId(), userId);
        validateCardOwner(request.getToCardId(), userId);
        cardOperationService.transfer(request);
    }

    @Override
    public CardDto findById(UUID cardId, UUID userId) {
        validateCardOwner(cardId, userId);
        return cardOperationService.findById(cardId);
    }

    @Override
    public BigDecimal getBalanceByCardId(UUID cardId, UUID userId) {
        validateCardOwner(cardId, userId);
        CardDto cardDto = cardOperationService.findById(cardId);
        return cardDto.getBalance();
    }

    private void validateCardOwner(UUID cardId, UUID ownerId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() ->
                new NotFoundException("карта с id" + cardId + " не найдена"));
        if (!card.getOwner().getId().equals(ownerId)) {
            throw new CardAccessException("пользователь " + ownerId + " не является владельцем карты " + cardId);
        }
    }
}
