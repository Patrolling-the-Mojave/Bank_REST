package com.example.bankcards.controller.user;

import com.example.bankcards.controller.ControllerTestBase;
import com.example.bankcards.dto.user.CreateUserRequestDto;
import com.example.bankcards.dto.user.UpdateUserAdminRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.user.UserAdminService;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminUserControllerTest extends ControllerTestBase {
    @MockBean
    private UserAdminService userAdminService;
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
                .id(userId)
                .username("testuser")
                .isActive((true))
                .roles(Set.of("ROLE_USER"))
                .build();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_success() throws Exception {
        CreateUserRequestDto request = CreateUserRequestDto.builder()
                .username("newuser")
                .password("password123")
                .roles(Set.of("ROLE_USER"))
                .build();

        UserDto createdUser = UserDto.builder()
                .username("newuser")
                .isActive(true)
                .roles(Set.of("ROLE_USER"))
                .build();

        when(userAdminService.createUser(eq(request))).thenReturn(createdUser);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(userAdminService).createUser(request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllUsers_success() throws Exception {
        Page<UserDto> userPage = new PageImpl<>(List.of(userDTO), PageRequest.of(0, 10), 1);
        when(userAdminService.findAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
        verify(userAdminService).findAllUsers(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findUserById_success() throws Exception {
        when(userAdminService.findUserById(userId)).thenReturn(userDTO);
        mockMvc.perform(get("/api/admin/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userAdminService).findUserById(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_success() throws Exception {
        UpdateUserAdminRequestDto request = UpdateUserAdminRequestDto.builder()
                .id(userId)
                .username("updateduser")
                .isActive(false)
                .build();

        UserDto updatedUser = UserDto.builder()
                .username("updateduser")
                .isActive(false)
                .roles(Set.of("ROLE_USER"))
                .build();

        when(userAdminService.updateUser(eq(request))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(userAdminService).updateUser(request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockUser_success() throws Exception {
        UserDto blockedUser = UserDto.builder()
                .username("testuser")
                .isActive(false)
                .roles(Set.of("ROLE_USER"))
                .build();

        when(userAdminService.blockUser(userId)).thenReturn(blockedUser);

        mockMvc.perform(post("/api/admin/users/{userId}/block", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(userAdminService).blockUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_success() throws Exception {
        doNothing().when(userAdminService).deleteUser(userId);
        mockMvc.perform(delete("/api/admin/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userAdminService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_validationError() throws Exception {
        CreateUserRequestDto invalidRequest = CreateUserRequestDto.builder().build();
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userAdminService, never()).createUser(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findUserById_notFound() throws Exception {
        UUID notFoundId = UUID.randomUUID();
        when(userAdminService.findUserById(notFoundId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/api/admin/users/{userId}", notFoundId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("not found"));
    }
}
