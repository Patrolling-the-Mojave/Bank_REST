package com.example.bankcards.controller.user;

import com.example.bankcards.controller.ControllerTestBase;
import com.example.bankcards.dto.auth.LoginRequestDto;
import com.example.bankcards.dto.user.RegisterUserRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.user.UserPrivateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest extends ControllerTestBase {
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserPrivateService userPrivateService;

    private String accessToken;
    private String refreshToken;
    private UserDto userDTO;

    @BeforeEach
    void setUp() {
        accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.token";
        refreshToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.refresh";

        userDTO = UserDto.builder()
                .username("testuser")
                .isActive(true)
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    @Test
    void register_success() throws Exception {
        RegisterUserRequestDto request = RegisterUserRequestDto.builder()
                .username("newuser")
                .password("password123")
                .build();
        userDTO.setUsername("newuser");
        Authentication authentication = mock(Authentication.class);

        when(userPrivateService.register(eq(request))).thenReturn(userDTO);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(authentication)).thenReturn(refreshToken);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken))
                .andExpect(jsonPath("$.user.username").value("newuser"))
                .andExpect(jsonPath("$.user.roles").isArray());

        verify(userPrivateService).register(request);
        verify(jwtTokenProvider).generateAccessToken(authentication);
    }

    @Test
    void login_success() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(authentication)).thenReturn(refreshToken);
        when(userPrivateService.getMyProfileById(any())).thenReturn(userDTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(accessToken))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authenticationManager).authenticate(
                argThat(token -> token instanceof UsernamePasswordAuthenticationToken)
        );
    }

    @Test
    void register_passwordTooShort() throws Exception {
        RegisterUserRequestDto request = RegisterUserRequestDto.builder()
                .username("newuser")
                .password("123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("ошибка валидации"));

        verify(userPrivateService, never()).register(any());
    }

    @Test
    void checkUsername_exists() throws Exception {
        when(userPrivateService.checkUsernameExists("existing")).thenReturn(true);
        mockMvc.perform(get("/api/auth/check-username")
                        .param("username", "existing"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userPrivateService).checkUsernameExists("existing");
    }
}
