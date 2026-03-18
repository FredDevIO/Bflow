package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.AuthRequest;
import io.github.lucyfred.bflow.dto.AuthResponse;
import io.github.lucyfred.bflow.dto.UserRequestDto;
import io.github.lucyfred.bflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User login endpoint")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with username and password")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register with username and password")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok((authService.signUp(userRequestDto)));
    }
}
