package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.RegisterUserRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.user.impl.UserOperationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserOperationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserOperationService operationsService;

    @Test
    void register_success() {
        RegisterUserRequestDto request = RegisterUserRequestDto.builder()
                .username("newuser")
                .password("plainPassword123")
                .build();

        String hashedPassword = "$2a$12$hashed";
        Role userRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_USER")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword123")).thenReturn(hashedPassword);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .password(hashedPassword)
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = operationsService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getRoles().contains("ROLE_USER"));

        verify(passwordEncoder).encode("plainPassword123");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals(hashedPassword) &&
                        user.getRoles().contains(userRole)
        ));
    }

    @Test
    void register_usernameExists() {
        RegisterUserRequestDto request = RegisterUserRequestDto.builder()
                .username("existing")
                .password("password")
                .build();

        when(userRepository.existsByUsername("existing")).thenReturn(true);
        assertThatThrownBy(() -> operationsService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("имя пользователя existing уже занято");

        verify(userRepository, never()).save(any());
    }

    @Test
    void blockUser_success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .isActive(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserDto result = operationsService.blockUser(userId);
        assertThat(result.getIsActive()).isFalse();
        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
    }
}
