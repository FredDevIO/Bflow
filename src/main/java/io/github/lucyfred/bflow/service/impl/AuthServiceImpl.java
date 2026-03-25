package io.github.lucyfred.bflow.service.impl;

import io.github.lucyfred.bflow.dto.AuthRequest;
import io.github.lucyfred.bflow.dto.AuthResponse;
import io.github.lucyfred.bflow.dto.UserRequestDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.exception.DuplicateResourceException;
import io.github.lucyfred.bflow.repository.UserRepository;
import io.github.lucyfred.bflow.security.JwtService;
import io.github.lucyfred.bflow.service.AuthService;
import io.github.lucyfred.bflow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );

        User user = (User) auth.getPrincipal();

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken);
    }

    @Override
    public AuthResponse signUp(UserRequestDto userRequestDto) {
        if(userRepository.existsByUsername(userRequestDto.username())) {
            throw new DuplicateResourceException("Username is already in use");
        }

        if(userRepository.existsByEmail(userRequestDto.email())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        User user = new User();
        user.setUsername(userRequestDto.username());
        user.setEmail(userRequestDto.email());

        user.setPassword(passwordEncoder.encode(userRequestDto.password()));

        userRepository.save(user);

        categoryService.createDefaultCategories(user);

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken);
    }
}
