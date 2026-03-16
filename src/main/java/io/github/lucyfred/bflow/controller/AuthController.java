package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.AuthRequest;
import io.github.lucyfred.bflow.dto.AuthResponse;
import io.github.lucyfred.bflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest.username(), authRequest.password()));
    }
}
