package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.BlockCardRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardAccessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.card.impl.CardOperationService;
import com.example.bankcards.service.card.impl.CardPrivateServiceImpl;
import com.example.bankcards.util.CardMaskUtil;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardPrivateServiceImplTest {
    @Mock
    private CardOperationService operationsService;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private EncryptionUtil encryptionUtil;
    @Mock
    private CardMaskUtil cardMaskingUtil;

    @InjectMocks
    private CardPrivateServiceImpl cardPrivateService;

    private UUID userId;
    private UUID cardId;
    private Card card;
    private User owner;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        owner = User.builder()
                .id(userId)
                .username("testuser")
                .password("hashed")
                .isActive(true)
                .roles(new HashSet<>())
                .build();

        card = Card.builder()
                .id(cardId)
                .encryptedNumber("encrypted_1234")
                .owner(owner)
                .expiryDate(LocalDate.of(2030, 12, 31))
                .status(Card.CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void findAllCardsByOwnerId_success() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(card), pageRequest, 1);

        CardDto expectedDto = CardDto.builder()
                .maskedNumber("**** **** **** 1234")
                .ownerName("testuser")
                .expiryDate(card.getExpiryDate())
                .status("ACTIVE")
                .balance(card.getBalance())
                .build();

        when(operationsService.findAllCardsByOwnerId(eq(userId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(expectedDto), pageRequest, 1));

        Page<CardDto> result = cardPrivateService.findAllCardsByOwnerId(userId, pageRequest);
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getOwnerName()).isEqualTo(expectedDto.getOwnerName());
        verify(operationsService).findAllCardsByOwnerId(eq(userId), eq(pageRequest));
    }

    @Test
    void blockCard_success() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        CardDto blockedDTO = CardDto.builder()
                .status("BLOCKED")
                .balance(card.getBalance())
                .build();

        when(operationsService.blockCard(cardId)).thenReturn(blockedDTO);
        BlockCardRequest blockRequest = BlockCardRequest.builder()
                .cardId(cardId)
                .ownerId(userId)
                .reason("reason")
                .build();
        CardDto result = cardPrivateService.blockCard(blockRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("BLOCKED");
        verify(cardRepository).findById(cardId);
        verify(operationsService).blockCard(cardId);
    }

    @Test
    void blockMyCard_accessDenied() {
        UUID otherUserId = UUID.randomUUID();
        User otherOwner = User.builder().id(otherUserId).build();
        Card otherCard = Card.builder().id(cardId).owner(otherOwner).build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(otherCard));

        BlockCardRequest blockCardRequest = BlockCardRequest.builder()
                .ownerId(userId)
                .cardId(cardId)
                .reason("reason")
                .build();
        assertThatThrownBy(() -> cardPrivateService.blockCard(blockCardRequest))
                .isInstanceOf(CardAccessException.class)
                .hasMessageContaining("не является владельцем");

        verify(operationsService, never()).blockCard(any());
    }

    @Test
    void transfer_success() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);

        TransferRequest request = TransferRequest.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .build();

        Card fromCard = Card.builder().id(fromCardId).owner(owner).balance(BigDecimal.valueOf(500)).build();
        Card toCard = Card.builder().id(toCardId).owner(owner).balance(BigDecimal.ZERO).build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        doNothing().when(operationsService).transfer(request);
        cardPrivateService.transfer(request, userId);
        verify(cardRepository).findById(fromCardId);
        verify(cardRepository).findById(toCardId);
        verify(operationsService).transfer(request);
    }

    @Test
    void transfer_accessDenied() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();
        User otherOwner = User.builder().id(UUID.randomUUID()).build();
        Card otherCard = Card.builder().id(toCardId).owner(otherOwner).build();

        TransferRequest request = TransferRequest.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(BigDecimal.valueOf(100))
                .build();

        Card myCard = Card.builder().id(fromCardId).owner(owner).build();

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(myCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(otherCard));

        assertThatThrownBy(() -> cardPrivateService.transfer(request, userId))
                .isInstanceOf(CardAccessException.class);

        verify(operationsService, never()).transfer(any());
    }
}
