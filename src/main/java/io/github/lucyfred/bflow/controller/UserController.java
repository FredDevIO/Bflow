package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.UserRequestDto;
import io.github.lucyfred.bflow.dto.UserResponseDto;
import io.github.lucyfred.bflow.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Register", description = "User registration and retrieve user info")
public class UserController {
    private final UserServiceImpl userService;

    @GetMapping("/username/{username}")
    @Operation(summary = "Search by username", description = "Search for a user by their username")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUserName(username));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Search by email", description = "Search for a user by their email")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping
    @Operation(summary = "User registration", description = "Register a new user in the system")
    public UserResponseDto createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        return userService.createUser(userRequestDto);
    }
}
