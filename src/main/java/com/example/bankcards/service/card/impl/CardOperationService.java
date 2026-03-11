package com.example.bankcards.service.card.impl;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequestDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNumberCreationException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.TransferException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMaskUtil;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardOperationService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskUtil cardMaskingUtil;

    @Transactional(readOnly = true)
    public Page<CardDto> findAllCardsByOwnerId(UUID ownerId, Pageable pageable) {
        return cardRepository.findAllByOwnerId(ownerId, pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CardDto> findAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public CardDto findById(UUID cardId) {
        return mapToDTO(getCardById(cardId));
    }

    @Transactional
    public CardDto blockCard(UUID cardId) {
        Card card = getCardById(cardId);
        card.setStatus(Card.CardStatus.BLOCKED);
        cardRepository.save(card);
        return mapToDTO(card);
    }

    @Transactional
    public CardDto activateCard(UUID cardId) {
        Card card = getCardById(cardId);
        card.setStatus(Card.CardStatus.ACTIVE);
        cardRepository.save(card);
        return mapToDTO(card);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        Card card = getCardById(cardId);
        cardRepository.deleteById(cardId);
    }


    @Transactional
    public void transfer(TransferRequest request) {
        Card fromCard = getCardById(request.getFromCardId());
        Card toCard = getCardById(request.getToCardId());

        if (fromCard.getId().equals(toCard.getId())) {
            throw new TransferException("нельзя перевести на ту же карту");
        }
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new TransferException("недостаточно средств");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        saveTransaction(fromCard, toCard, request.getAmount());
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Transactional
    public CardDto createCard(CreateCardRequestDto request) {
        User owner = userRepository.findById(request.getOwnerId()).orElseThrow(() ->
                new NotFoundException("пользователь с id " + request.getOwnerId() + " не найден"));

        String cardNumber = generateCardNumber();
        String encryptedCardNumber = encryptionUtil.encrypt(cardNumber);
        if (cardRepository.existsByEncryptedNumber(encryptedCardNumber)) {
            throw new CardNumberCreationException("не удалось создать уникальный номер карты");
        }
        Card card = Card.builder()
                .id(UUID.randomUUID())
                .encryptedNumber(encryptedCardNumber)
                .owner(owner)
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(request.getExpiryDate())
                .balance(BigDecimal.ZERO)
                .build();
        cardRepository.save(card);

        return mapToDTO(card);
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }

    private Card getCardById(UUID cardId) {
        return cardRepository.findById(cardId).orElseThrow(() ->
                new NotFoundException("карта с id " + cardId + " не найдена"));
    }

    private void saveTransaction(Card fromCard, Card toCard, BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    private CardDto mapToDTO(Card card) {
        String decrypted = encryptionUtil.decrypt(card.getEncryptedNumber());
        return CardDto.builder()
                .id(card.getId())
                .maskedNumber(cardMaskingUtil.maskCardNumber(decrypted))
                .ownerName(card.getOwner().getUsername())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus().name())
                .balance(card.getBalance())
                .build();
    }
}
