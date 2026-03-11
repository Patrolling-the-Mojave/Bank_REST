package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.CreateUserRequestDto;
import com.example.bankcards.dto.user.UpdateUserAdminRequestDto;
import com.example.bankcards.dto.user.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserAdminService {

    UserDto createUser(CreateUserRequestDto newUser);

    Page<UserDto> findAllUsers(Pageable pageable);

    UserDto updateUser(UpdateUserAdminRequestDto updatedUser);

    UserDto findUserByUsername(String username);

    UserDto findUserById(UUID id);

    void deleteUser(UUID id);

    UserDto blockUser(UUID id);

    UserDto activateUser(UUID id);

}
