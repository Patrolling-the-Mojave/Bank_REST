package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequestDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.card.impl.CardOperationService;
import com.example.bankcards.util.CardMaskUtil;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardOperationServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private EncryptionUtil encryptionUtil;
    @Mock
    private CardMaskUtil cardMaskingUtil;

    @InjectMocks
    private CardOperationService operationsService;

    @Test
    void createCard_success() {
        UUID ownerId = UUID.randomUUID();
        CreateCardRequestDto request = CreateCardRequestDto.builder()
                .ownerId(ownerId)
                .expiryDate(LocalDate.of(2030, 12, 31))
                .build();

        User owner = User.builder().id(ownerId).username("testuser").build();
        String rawCardNumber = "4111111111111111";
        String encryptedNumber = "encrypted_data";

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(encryptionUtil.encrypt(anyString())).thenReturn(encryptedNumber);
        when(encryptionUtil.decrypt(encryptedNumber)).thenReturn(rawCardNumber);
        when(cardMaskingUtil.maskCardNumber(rawCardNumber)).thenReturn("**** **** **** 1111");

        Card savedCard = Card.builder()
                .id(UUID.randomUUID())
                .encryptedNumber(encryptedNumber)
                .owner(owner)
                .expiryDate(request.getExpiryDate())
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardDto result = operationsService.createCard(request);

        assertThat(result).isNotNull();
        assertThat(result.getMaskedNumber()).isEqualTo("**** **** **** 1111");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");

        verify(userRepository).findById(ownerId);
        verify(encryptionUtil).encrypt(anyString());
        verify(cardRepository).save(argThat(card ->
                card.getEncryptedNumber().equals(encryptedNumber) &&
                        card.getBalance().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    void transfer_success() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);

        Card fromCard = Card.builder()
                .id(fromId)
                .balance(BigDecimal.valueOf(500))
                .status(Card.CardStatus.ACTIVE)
                .build();

        Card toCard = Card.builder()
                .id(toId)
                .balance(BigDecimal.valueOf(200))
                .status(Card.CardStatus.ACTIVE)
                .build();

        TransferRequest request = TransferRequest.builder()
                .fromCardId(fromId)
                .toCardId(toId)
                .amount(amount)
                .build();

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        operationsService.transfer(request);
        assertThat(fromCard.getBalance()).isEqualTo(BigDecimal.valueOf(400));
        assertThat(toCard.getBalance()).isEqualTo(BigDecimal.valueOf(300));
        verify(cardRepository).save(fromCard);
        verify(cardRepository).save(toCard);
        verify(transactionRepository).save(argThat(tx ->
                tx.getAmount().compareTo(amount) == 0 &&
                        tx.getFromCard().getId().equals(fromId) &&
                        tx.getToCard().getId().equals(toId)
        ));
    }

    @Test
    void transfer_insufficientFunds() {
        Card fromCard = Card.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(50))
                .build();

        Card toCard = Card.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .build();

        TransferRequest request = TransferRequest.builder()
                .fromCardId(fromCard.getId())
                .toCardId(toCard.getId())
                .amount(BigDecimal.valueOf(100))
                .build();

        when(cardRepository.findById(fromCard.getId())).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCard.getId())).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> operationsService.transfer(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("недостаточно средств");
        verify(cardRepository, never()).save(any());
    }
}
