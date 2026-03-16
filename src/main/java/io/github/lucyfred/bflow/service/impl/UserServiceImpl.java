package io.github.lucyfred.bflow.service.impl;

import io.github.lucyfred.bflow.dto.UserRequestDto;
import io.github.lucyfred.bflow.dto.UserResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.exception.DuplicateResourceException;
import io.github.lucyfred.bflow.exception.ResourceNotFoundException;
import io.github.lucyfred.bflow.mapper.UserMapper;
import io.github.lucyfred.bflow.repository.UserRepository;
import io.github.lucyfred.bflow.service.CategoryService;
import io.github.lucyfred.bflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CategoryService categoryService;
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
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        if(userRepository.findByUsername(userRequestDto.username()).isPresent()){
            throw new DuplicateResourceException("Username is already taken");
        }

        if(userRepository.findByEmail(userRequestDto.email()).isPresent()) {
            throw new DuplicateResourceException("Email is already taken");
        }

        User user = userMapper.toUserFromRequest(userRequestDto);

        String hashedPassword = passwordEncoder.encode(userRequestDto.password());
        user.setPassword(hashedPassword);

        User saved = userRepository.save(user);

        categoryService.createDefaultCategories(saved);

        return userMapper.toUserResponseDto(saved);
    }
}
