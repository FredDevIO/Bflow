package io.github.lucyfred.bflow.service;

import io.github.lucyfred.bflow.dto.UserResponseDto;

public interface UserService {
    UserResponseDto getUserByUserName(String username);
    UserResponseDto getUserByEmail(String email);
}
