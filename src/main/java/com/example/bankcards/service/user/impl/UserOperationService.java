package com.example.bankcards.service.user.impl;

import com.example.bankcards.dto.user.CreateUserRequestDto;
import com.example.bankcards.dto.user.RegisterUserRequestDto;
import com.example.bankcards.dto.user.UpdateUserAdminRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOperationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDto findById(UUID userId) {
        return mapToDto(getUserById(userId));
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public UserDto findUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new NotFoundException("пользователь с именем " + username + " не найден"));
        return mapToDto(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequestDto creationRequest) {
        if (userRepository.existsByUsername(creationRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("имя пользователя " + creationRequest.getUsername() + " уже занято");
        }
        Set<Role> roles = creationRequest.getRoles()
                .stream()
                .map(this::getRoleByName)
                .collect(Collectors.toSet());

        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .password(passwordEncoder.encode(creationRequest.getPassword()))
                .roles(roles)
                .username(creationRequest.getUsername())
                .build();

        userRepository.save(user);
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateUser(UpdateUserAdminRequestDto updateRequest) {
        User user = getUserById(updateRequest.getId());
        if (updateRequest.getUsername() != null) {
            if (!user.getUsername().equals(updateRequest.getUsername()) &&
                    userRepository.existsByUsername(updateRequest.getUsername())) {
                throw new UsernameAlreadyExistsException("пользователь с таким именем уже существует");
            }
            user.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        if (updateRequest.getRoles() != null) {
            Set<Role> roles = updateRequest.getRoles().stream().map(this::getRoleByName).collect(Collectors.toSet());
            user.setRoles(roles);
        }
        if (updateRequest.getIsActive() != null) {
            user.setIsActive(updateRequest.getIsActive());
        }
        userRepository.save(user);

        return mapToDto(user);
    }

    @Transactional
    public UserDto register(RegisterUserRequestDto registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("имя пользователя " + registerRequest.getUsername() + " уже занято");
        }
        Role role = getRoleByName("ROLE_USER");
        User user = User.builder()
                .id(UUID.randomUUID())
                .roles(Set.of(role))
                .isActive(true)
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .build();
        userRepository.save(user);
        return mapToDto(user);
    }

    @Transactional
    public UserDto blockUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
        return mapToDto(user);
    }

    @Transactional
    public UserDto activateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        userRepository.save(user);
        return mapToDto(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = getUserById(userId);
        userRepository.deleteById(userId);
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("пользователь с id " + userId + " не найден"));
    }

    private Role getRoleByName(String roleName) {
        return roleRepository.findByName(roleName).orElseThrow(() ->
                new NotFoundException("роль " + roleName + " не найдена"));
    }

    private UserDto mapToDto(User user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .isActive(user.getIsActive())
                .roles(roles)
                .build();

    }
}
