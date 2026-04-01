package io.github.lucyfred.bflow.service.impl;

import io.github.lucyfred.bflow.dto.AdminUserResponseDto;
import io.github.lucyfred.bflow.dto.ChangePasswordRequest;
import io.github.lucyfred.bflow.dto.UserResponseDto;
import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.enums.Currency;
import io.github.lucyfred.bflow.enums.Language;
import io.github.lucyfred.bflow.enums.Role;
import io.github.lucyfred.bflow.exception.ResourceNotFoundException;
import io.github.lucyfred.bflow.mapper.UserMapper;
import io.github.lucyfred.bflow.repository.UserRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("testuser@gmail.com");
        mockUser.setPassword("oldPass");
        mockUser.setCurrency(Currency.USD);
        mockUser.setLanguage(Language.ENGLISH);
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void getUserByUserName_success() {
        UserResponseDto expected = new UserResponseDto("testUser", "testuser@gmail.com", "USD", "ENGLISH", "USER");
        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserResponseDto(mockUser)).thenReturn(expected);

        UserResponseDto response = userService.getUserByUserName(mockUser.getUsername());

        assertNotNull(response);
        assertEquals(mockUser.getUsername(), response.username());
        assertEquals(mockUser.getEmail(), response.email());
        verify(userRepository, times(1)).findByUsername(mockUser.getUsername());
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void getUserByEmail_success() {
        UserResponseDto expected = new UserResponseDto("testUser", "testuser@gmail.com", "USD", "ENGLISH", "USER");
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserResponseDto(mockUser)).thenReturn(expected);

        UserResponseDto response = userService.getUserByEmail(mockUser.getEmail());

        assertNotNull(response);
        assertEquals(mockUser.getEmail(), response.email());
        verify(userRepository, times(1)).findByEmail(mockUser.getEmail());
    }

    @Test
    @DisplayName("Should get my profile successfully")
    void getMyProfile_success() {
        UserResponseDto expected = new UserResponseDto("testUser", "testuser@gmail.com", "USD", "ENGLISH", "USER");
        Long idRequested = 1L;
        Long idUser = mockUser.getId();
        when(userRepository.findById(idRequested)).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserResponseDto(mockUser)).thenReturn(expected);

        UserResponseDto response = userService.getMyProfile(idRequested, idUser);

        assertNotNull(response);
        assertEquals(response.username(), expected.username());
        assertEquals(response.email(), expected.email());
        verify(userRepository, times(1)).findById(idRequested);
    }

    @Test
    @DisplayName("Should get all users successfully")
    void getAllUsers_success() {
        User user = User.builder()
                .id(1L)
                .username("testUser")
                .email("testuser@gmail.com")
                .currency(Currency.USD)
                .language(Language.ENGLISH)
                .role(Role.USER)
                .build();

        AdminUserResponseDto adminUser = new AdminUserResponseDto(1L, "testUser", "testuser@gmail.com", "USD", "ENGLISH", "USER");
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> users = new PageImpl<>(List.of(user));
        when(userRepository.findAllByIdNot(mockUser.getId(), pageable)).thenReturn(users);
        when(userMapper.toAdminUserResponseDto(any(User.class))).thenReturn(adminUser);

        Page<AdminUserResponseDto> response = userService.getAllUsers(mockUser.getId(), pageable);

        assertNotNull(response);
        assertEquals(response.getSize(), users.getSize());
        assertEquals(response.getContent().get(0).username(), user.getUsername());
        verify(userRepository, times(1)).findAllByIdNot(mockUser.getId(), pageable);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(mockUser);
    }

    @Test
    @DisplayName("Should update profile successfully")
    void updateProfile_success() {
        UserResponseDto expected = new UserResponseDto("newUser", "newemail@test.com", "EUR", "SPANISH", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserResponseDto(mockUser)).thenReturn(expected);

        UserResponseDto result = userService.updateProfile(1L, "newUser", "newemail@test.com", "EUR", "SPANISH");

        assertNotNull(result);
        assertEquals("newUser", result.username());
        assertEquals("newemail@test.com", result.email());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("Should update password successfully")
    void changePassword_success() {
        String oldPass = "oldPass";
        String newPass = "newPass12345";
        String encodedNew = "encodedNewPass";
        String initialPassword = mockUser.getPassword();

        ChangePasswordRequest request = new ChangePasswordRequest(oldPass, newPass);
        UserResponseDto expected = new UserResponseDto("testUser", "testuser@gmail.com", "USD", "ENGLISH", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(oldPass, initialPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPass)).thenReturn(encodedNew);
        when(userMapper.toUserResponseDto(mockUser)).thenReturn(expected);

        UserResponseDto result = userService.changePassword(1L, request);

        assertNotNull(result);
        assertEquals("testUser", result.username());
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches(oldPass, initialPassword);
        verify(passwordEncoder, times(1)).encode("newPass12345");
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("Admin should update password to an user successfully")
    void updateUserPassword_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("newPass12345")).thenReturn("encodedNewPass");

        boolean result = userService.updateUserPassword(1L, "newPass12345");

        assertTrue(result);
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newPass12345");
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("Should not find the user")
    void getUserByUserName_userNotFound(){
        String username = "user";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByUserName(username);
        });
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should not find user by email")
    void getUserByEmail_userNotFound(){
        String email = "notfound@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByEmail(email);
        });
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should not get profile when user not found")
    void getMyProfile_userNotFound(){
        Long idRequested = 1L;
        Long idUser = 1L;

        when(userRepository.findById(idRequested)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getMyProfile(idRequested, idUser);
        });
        verify(userRepository, times(1)).findById(idRequested);
    }

    @Test
    @DisplayName("Should deny access when user tries to access another profile")
    void getMyProfile_accessDenied(){
        Long idRequested = 1L;
        Long idUser = 2L;

        assertThrows(AccessDeniedException.class, () -> {
            userService.getMyProfile(idRequested, idUser);
        });
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should not delete user when not found")
    void deleteUser_userNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Should not update profile when user not found")
    void updateProfile_userNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateProfile(1L, "newUser", "newemail@test.com", "EUR", "SPANISH");
        });
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should not change password when user not found")
    void changePassword_userNotFound(){
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.changePassword(1L, request);
        });
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should not change password when current password does not match")
    void changePassword_passwordMismatch(){
        String oldPass = "wrongPass";
        String newPass = "newPass12345";

        ChangePasswordRequest request = new ChangePasswordRequest(oldPass, newPass);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(oldPass, mockUser.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            userService.changePassword(1L, request);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches(oldPass, mockUser.getPassword());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should not update user password when user not found")
    void updateUserPassword_userNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserPassword(1L, "newPass12345");
        });
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}