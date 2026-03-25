package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.*;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Register", description = "User registration and retrieve user info")
public class UserController {
    private final UserServiceImpl userService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Retrieve my user data")
    public ResponseEntity<UserResponseDto> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getMyProfile(user.getId(), user.getId()));
    }

    @PostMapping("/me/profile")
    @Operation(summary = "Update my profile", description = "Update my user data")
    public ResponseEntity<UserResponseDto> updateProfile(@Valid @RequestBody ChangeProfileRequest changeProfileRequest, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), changeProfileRequest.newUsername(), changeProfileRequest.newEmail(), changeProfileRequest.currency(), changeProfileRequest.language()));
    }

    @PostMapping("/me/password")
    @Operation(summary = "Change password", description = "Change my password")
    public ResponseEntity<UserResponseDto> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.changePassword(user.getId(), changePasswordRequest.oldPassword(), changePasswordRequest.newPassword()));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users", description = "Retrieve all users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserResponseDto> getAllUsers(@PageableDefault(size = 10, page = 0) Pageable pageable, @AuthenticationPrincipal User user) {
        return userService.getAllUsers(user.getId(), pageable);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteUser(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    @PostMapping("/{id}/password")
    @Operation(summary = "Update user password", description = "Update a user password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserPassword(@PathVariable("id") Long userId, @Valid @RequestBody AdminChangePasswordRequest adminChangePasswordRequest, @AuthenticationPrincipal User user) {
        userService.updateUserPassword(userId, adminChangePasswordRequest.newPassword());
        return ResponseEntity.ok().build();
    }
}
