package io.github.lucyfred.bflow.service;

import io.github.lucyfred.bflow.dto.AdminUserResponseDto;
import io.github.lucyfred.bflow.dto.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDto getUserByUserName(String username);
    UserResponseDto getUserByEmail(String email);
    UserResponseDto getMyProfile(Long idRequested, Long authenticatedUserId);
    Page<AdminUserResponseDto> getAllUsers(Long id, Pageable pageable);
    boolean deleteUser(Long userId);
    UserResponseDto updateProfile(Long userId, String newUsername, String newEmail, String currency, String language);
    UserResponseDto changePassword(Long userId, String oldPassword, String newPassword);
    boolean updateUserPassword(Long userId, String newPassword);
}
