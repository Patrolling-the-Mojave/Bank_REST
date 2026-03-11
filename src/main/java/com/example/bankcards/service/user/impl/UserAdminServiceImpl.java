package com.example.bankcards.service.user.impl;

import com.example.bankcards.dto.user.CreateUserRequestDto;
import com.example.bankcards.dto.user.UpdateUserAdminRequestDto;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.service.user.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {
    private final UserOperationService userOperationService;

    @Override
    public UserDto activateUser(UUID id) {
        return userOperationService.activateUser(id);
    }

    @Override
    public UserDto createUser(CreateUserRequestDto newUser) {
        return userOperationService.createUser(newUser);
    }

    @Override
    public Page<UserDto> findAllUsers(Pageable pageable) {
        return userOperationService.findAllUsers(pageable);
    }

    @Override
    public UserDto updateUser(UpdateUserAdminRequestDto updatedUser) {
        return userOperationService.updateUser(updatedUser);
    }

    @Override
    public UserDto findUserByUsername(String username) {
        return userOperationService.findUserByUsername(username);
    }

    @Override
    public UserDto findUserById(UUID id) {
        return userOperationService.findById(id);
    }

    @Override
    public void deleteUser(UUID id) {
        userOperationService.deleteUser(id);
    }

    @Override
    public UserDto blockUser(UUID id) {
        return userOperationService.blockUser(id);
    }
}
