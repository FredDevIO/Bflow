package io.github.lucyfred.bflow.service;

import io.github.lucyfred.bflow.dto.AuthResponse;

public interface AuthService {
    public AuthResponse login(String username, String password);
}
