package com.example.bankcards.controller.card;

import com.example.bankcards.controller.ControllerTestBase;
import com.example.bankcards.dto.card.BlockCardRequest;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.exception.CardAccessException;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.card.CardPrivateService;
import com.example.bankcards.service.user.UserPrivateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateCardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PrivateCardControllerTest extends ControllerTestBase {
    @MockBean
    private CardPrivateService cardPrivateService;
    @MockBean
    private UserPrivateService userPrivateService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UUID userId;
    private UUID cardId;
    private CardDto cardDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        cardDTO = CardDto.builder()
                .id(cardId)
                .maskedNumber("**** **** **** 1234")
                .ownerName("testuser")
                .expiryDate(LocalDate.of(2030, 12, 31))
                .status("ACTIVE")
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(userPrivateService.getCurrentUserIdByUsername(cardDTO.getOwnerName())).thenReturn(userId);
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("user");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyCards_success() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<CardDto> cardPage = new PageImpl<>(List.of(cardDTO), pageRequest, 1);

        when(cardPrivateService.findAllCardsByOwnerId(eq(userId), any(PageRequest.class)))
                .thenReturn(cardPage);

        mockMvc.perform(get("/api/private/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andDo(print());

        verify(cardPrivateService).findAllCardsByOwnerId(eq(userId), any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyCard_success() throws Exception {
        when(cardPrivateService.findById(eq(cardId), eq(userId))).thenReturn(cardDTO);

        mockMvc.perform(get("/api/private/cards/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.balance").value(1000));

        verify(cardPrivateService).findById(cardId, userId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void blockMyCard_success() throws Exception {
        CardDto blockedDTO = CardDto.builder()
                .status("BLOCKED")
                .balance(cardDTO.getBalance())
                .build();

        BlockCardRequest blockCardRequest = BlockCardRequest.builder()
                .reason("reason")
                .ownerId(userId)
                .cardId(cardId)
                .build();

        when(cardPrivateService.blockCard(eq(blockCardRequest))).thenReturn(blockedDTO);

        mockMvc.perform(post("/api/private/cards/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(blockCardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(cardPrivateService).blockCard(blockCardRequest);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void transfer_success() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromCardId(UUID.randomUUID())
                .toCardId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(100))
                .build();

        doNothing().when(cardPrivateService).transfer(eq(request), eq(userId));

        mockMvc.perform(post("/api/private/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk());

        verify(cardPrivateService).transfer(request, userId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void transfer_validationError() throws Exception {
        TransferRequest invalidRequest = TransferRequest.builder()
                .build();

        mockMvc.perform(post("/api/private/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("ошибка валидации"));

        verify(cardPrivateService, never()).transfer(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyCard_accessDenied() throws Exception {
        when(cardPrivateService.findById(eq(cardId), eq(userId)))
                .thenThrow(new CardAccessException("Нет доступа"));

        mockMvc.perform(get("/api/private/cards/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.name").value("нет доступа к данной карте"));
    }
}
