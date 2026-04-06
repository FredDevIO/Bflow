package io.github.lucyfred.bflow.service.impl;

import io.github.lucyfred.bflow.dto.AuthRequest;
import io.github.lucyfred.bflow.dto.UserRequestDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.exception.DuplicateResourceException;
import io.github.lucyfred.bflow.repository.UserRepository;
import io.github.lucyfred.bflow.security.JwtService;
import io.github.lucyfred.bflow.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CategoryService categoryService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks private AuthServiceImpl authService;

    @Test
    @DisplayName("Scenario: Successfully registration of a new user")
    void successfulRegistration() {
        UserRequestDto request = new UserRequestDto("Jhon", "jhon@gmail.com", "123456");
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(jwtService.generateToken(any())).thenReturn("emulated-token");

        var response = authService.signUp(request);

        assertNotNull(response.token());
        assertEquals("emulated-token", response.token());

        verify(userRepository, times(1)).save(any(User.class));
        verify(categoryService, times(1)).createDefaultCategories(any(User.class));
    }

    @Test
    @DisplayName("Scenario: Registration with existing username")
    void registrationWithExistingUsername() {
        UserRequestDto request = new UserRequestDto("Jhon", "jhon@gmail.com", "123456");
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.signUp(request));

        verify(userRepository, never()).save(any(User.class));
        verify(categoryService, never()).createDefaultCategories(any(User.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Scenario: Registration with existing email")
    void registrationWithExistingEmail() {
        UserRequestDto request = new UserRequestDto("Jhon", "jhon@gmail.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.signUp(request));

        verify(userRepository, never()).save(any(User.class));
        verify(categoryService, never()).createDefaultCategories(any(User.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Scenario: Successfully login with valid credentials")
    void successfulLogin(){
        AuthRequest request = new AuthRequest("Jhon", "123456");
        User mockUser = new User();
        mockUser.setUsername("Jhon");

        Authentication mockAuth = mock(org.springframework.security.core.Authentication.class);

        when(mockAuth.getPrincipal()).thenReturn(mockUser);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtService.generateToken(any())).thenReturn("emulated-token");

        var response = authService.login(request);

        assertNotNull(response.token());
        assertEquals("emulated-token", response.token());

        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("Scenario: Login with invalid credentials")
    void loginWithInvalidCredentials(){
        AuthRequest request = new AuthRequest("Jhon", "123456");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(any());
        verify(userRepository, never()).save(any(User.class));

    }

}