package io.github.lucyfred.bflow.service;

import io.github.lucyfred.bflow.dto.AuthRequest;
import io.github.lucyfred.bflow.dto.AuthResponse;
import io.github.lucyfred.bflow.dto.UserRequestDto;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    AuthResponse signUp(UserRequestDto userRequestDto);
}
