package com.example.bankcards.controller.card;

import com.example.bankcards.controller.ControllerTestBase;
import com.example.bankcards.dto.card.CardDto;
import com.example.bankcards.dto.card.CreateCardRequestDto;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.card.CardAdminService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminCardControllerTest extends ControllerTestBase {
    @MockBean
    private CardAdminService cardAdminService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UUID cardId;
    private CardDto cardDTO;

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();
        cardDTO = CardDto.builder()
                .id(cardId)
                .expiryDate(LocalDate.of(2030, 12, 31))
                .maskedNumber("**** **** **** 1234")
                .status("ACTIVE")
                .balance(BigDecimal.valueOf(1000))
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("admin");
    }

    private String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCard_admin_success() throws Exception {
        CreateCardRequestDto request = CreateCardRequestDto.builder()
                .ownerId(UUID.randomUUID())
                .expiryDate(LocalDate.of(2030, 12, 31))
                .build();

        when(cardAdminService.createCard(any(CreateCardRequestDto.class))).thenReturn(cardDTO);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardAdminService).createCard(request);
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllCards_admin_success() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<CardDto> cardPage = new PageImpl<>(List.of(cardDTO), pageRequest, 1);

        when(cardAdminService.findAllCards(any(PageRequest.class))).thenReturn(cardPage);

        mockMvc.perform(get("/api/admin/cards")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(cardId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void blockCard_admin_success() throws Exception {
        CardDto blockedDTO = CardDto.builder()
                .id(cardId)
                .status("BLOCKED")
                .build();

        when(cardAdminService.blockCard(cardId)).thenReturn(blockedDTO);

        mockMvc.perform(post("/api/admin/cards/{cardId}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(cardAdminService).blockCard(cardId);
    }
}
