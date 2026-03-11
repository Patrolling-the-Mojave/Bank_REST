package com.example.bankcards.controller.user;

import com.example.bankcards.controller.ControllerTestBase;
import com.example.bankcards.dto.user.UpdateUserClientRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.user.UserPrivateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateUserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PrivateUserControllerTest extends ControllerTestBase {
    @MockBean
    private UserPrivateService userPrivateService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UUID userId;
    private UserDto userDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userDTO = UserDto.builder()
                .username("testuser")
                .isActive(true)
                .roles(Set.of("ROLE_USER"))
                .build();
        when(userPrivateService.getCurrentUserIdByUsername("testuser")).thenReturn(userId);
        when(userPrivateService.getCurrentUserIdByUsername("user")).thenReturn(userId);
        when(userPrivateService.getCurrentUserIdByUsername("admin")).thenReturn(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyProfile_success() throws Exception {
        when(userPrivateService.getMyProfileById(userId)).thenReturn(userDTO);
        mockMvc.perform(get("/api/private/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(userPrivateService).getMyProfileById(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateMyProfile_success() throws Exception {
        UpdateUserClientRequestDto request = UpdateUserClientRequestDto.builder()
                .id(userId)
                .username("newusername")
                .build();

        UserDto updatedUser = UserDto.builder()
                .id(userId)
                .username("newusername")
                .isActive(true)
                .roles(Set.of("ROLE_USER"))
                .build();

        when(userPrivateService.updateProfile(eq(request))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/private/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"));

        verify(userPrivateService).updateProfile(request);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateMyProfile_passwordTooShort() throws Exception {
        UpdateUserClientRequestDto request = UpdateUserClientRequestDto.builder()
                .id(userId)
                .password("123")
                .build();

        mockMvc.perform(put("/api/private/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("ошибка валидации"));


        verify(userPrivateService, never()).updateProfile(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMyProfile_admin_access() throws Exception {
        when(userPrivateService.getMyProfileById(userId)).thenReturn(userDTO);

        mockMvc.perform(get("/api/private/users"))
                .andExpect(status().isOk());

        verify(userPrivateService).getMyProfileById(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyProfile_notFound() throws Exception {
        when(userPrivateService.getMyProfileById(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/api/private/users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь не найден"));
    }
}
