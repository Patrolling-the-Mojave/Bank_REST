package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.RegisterUserRequestDto;
import com.example.bankcards.dto.user.UpdateUserClientRequestDto;
import com.example.bankcards.dto.user.UserDto;

import java.util.UUID;

public interface UserPrivateService {
    UserDto register(RegisterUserRequestDto registerRequest);

    UserDto getMyProfileById(UUID userId);

    UUID getCurrentUserIdByUsername(String username);

    UserDto updateProfile(UpdateUserClientRequestDto updateRequest);

    Boolean checkUsernameExists(String username);
}
