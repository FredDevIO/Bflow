package io.github.lucyfred.bflow.service.impl;

import io.github.lucyfred.bflow.dto.AdminUserResponseDto;
import io.github.lucyfred.bflow.dto.UserResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.enums.Currency;
import io.github.lucyfred.bflow.enums.Language;
import io.github.lucyfred.bflow.exception.ResourceNotFoundException;
import io.github.lucyfred.bflow.mapper.UserMapper;
import io.github.lucyfred.bflow.repository.UserRepository;
import io.github.lucyfred.bflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto getUserByUserName(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found"));

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email " + email + " not found"));

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto getMyProfile(Long idRequested, Long authenticatedUserId) {
        if(!idRequested.equals(authenticatedUserId)){
            throw new AccessDeniedException("Access Denied");
        }

        User user = userRepository.findById(idRequested)
                .orElseThrow(() -> new ResourceNotFoundException("User " + idRequested + " not found"));

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public Page<AdminUserResponseDto> getAllUsers(Long id, Pageable pageable) {
        Page<User> users = userRepository.findAllByIdNot(id, pageable);
        return users.map(userMapper::toAdminUserResponseDto);
    }

    @Override
    public boolean deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));

        userRepository.delete(user);

        return true;
    }

    @Override
    public UserResponseDto updateProfile(Long userId, String newUsername, String newEmail, String currency, String language) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));

        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setCurrency(Currency.valueOf(currency));
        user.setLanguage(Language.valueOf(language));

        userRepository.save(user);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));

        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public boolean updateUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));

        String pass = passwordEncoder.encode(newPassword);

        user.setPassword(pass);

        userRepository.save(user);

        return true;
    }
}
