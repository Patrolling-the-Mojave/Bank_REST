package com.example.bankcards.service.user.impl;

import com.example.bankcards.dto.user.RegisterUserRequestDto;
import com.example.bankcards.dto.user.UpdateUserAdminRequestDto;
import com.example.bankcards.dto.user.UpdateUserClientRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.user.UserPrivateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPrivateServiceImpl implements UserPrivateService {
    private final UserOperationService userOperationService;
    private final UserRepository userRepository;

    @Override
    public UserDto getMyProfileById(UUID userId) {
        return userOperationService.findById(userId);
    }

    @Override
    public UserDto register(RegisterUserRequestDto registerRequest) {
        return userOperationService.register(registerRequest);
    }

    @Override
    public Boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public UUID getCurrentUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new NotFoundException("пользователь с именем " + username + " не найден"));
        return user.getId();
    }

    @Override
    public UserDto updateProfile(UpdateUserClientRequestDto updateRequest) {
        UpdateUserAdminRequestDto adminRequest = UpdateUserAdminRequestDto.builder()
                .id(updateRequest.getId())
                .username(updateRequest.getUsername())
                .password(updateRequest.getPassword())
                .build();
        return userOperationService.updateUser(adminRequest);
    }
}
