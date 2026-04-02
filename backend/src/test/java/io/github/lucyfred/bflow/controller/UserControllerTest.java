package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.dto.*;
import io.github.lucyfred.bflow.repository.UserRepository;
import io.github.lucyfred.bflow.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import java.util.List;

import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.exception.ResourceNotFoundException;
import io.github.lucyfred.bflow.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
    }

    @Test
    @DisplayName("Should return my profile successfully")
    void getMyProfile_success() throws Exception {
        UserResponseDto user = new UserResponseDto("testUser", "testuser@gmail.com", "USD", "ENGLISH", "ADMIN");

        when(userService.getMyProfile(any(), any())).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("Should update profile successfully")
    void updateProfile_success() throws Exception {
        ChangeProfileRequest request = new ChangeProfileRequest("newUsername", "newemail@test.com", "USD", "ENGLISH");

        UserResponseDto user = new UserResponseDto("newUsername", "newemail@test.com", "USD", "ENGLISH", "USER");

        when(userService.updateProfile(any(), any(), any(), any(), any())).thenReturn(user);

        mockMvc.perform(post("/api/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUsername"))
                .andExpect(jsonPath("$.email").value("newemail@test.com"));
    }

    @Test
    @DisplayName("Should change password successfully")
    void changePassword_success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("1234", "12345678");
        UserResponseDto response = new UserResponseDto("testUser", "testuser@gmail.com", "USD", "ENGLISH", "USER");

        when(userService.changePassword(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.language").value("ENGLISH"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Should return all users successfully")
    void getAllUsers_success() throws Exception {
        AdminUserResponseDto adminUser = new AdminUserResponseDto(2L, "otherUser", "other@test.com", "USD", "ENGLISH", "USER");
        Page<AdminUserResponseDto> page = new PageImpl<>(List.of(adminUser));

        when(userService.getAllUsers(anyLong(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/all")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("otherUser"))
                .andExpect(jsonPath("$.content[0].email").value("other@test.com"));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_success() throws Exception {
        when(userService.deleteUser(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/users/2")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should update user password successfully")
    void updateUserPassword_success() throws Exception {
        AdminChangePasswordRequest request = new AdminChangePasswordRequest("newPassword123");

        when(userService.updateUserPassword(anyLong(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/users/2/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when accessing another user's profile")
    void getMyProfile_accessDenied() throws Exception {
        when(userService.getMyProfile(anyLong(), anyLong())).thenThrow(new AccessDeniedException("Access Denied"));

        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void getMyProfile_userNotFound() throws Exception {
        when(userService.getMyProfile(anyLong(), anyLong())).thenThrow(new ResourceNotFoundException("User 1 not found"));

        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should throw RuntimeException when old password does not match")
    void changePassword_wrongOldPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongOldPass", "newPass123");

        when(userService.changePassword(anyLong(), any(ChangePasswordRequest.class))).thenThrow(new RuntimeException("Current password does not match"));

        mockMvc.perform(post("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void changePassword_userNotFound() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass123");

        when(userService.changePassword(anyLong(), any(ChangePasswordRequest.class))).thenThrow(new ResourceNotFoundException("User 1 not found"));

        mockMvc.perform(post("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user to delete not found")
    void deleteUser_userNotFound() throws Exception {
        when(userService.deleteUser(anyLong())).thenThrow(new ResourceNotFoundException("User 2 not found"));

        mockMvc.perform(delete("/api/users/2")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user to update password not found")
    void updateUserPassword_userNotFound() throws Exception {
        AdminChangePasswordRequest request = new AdminChangePasswordRequest("newPassword123");

        when(userService.updateUserPassword(anyLong(), anyString())).thenThrow(new ResourceNotFoundException("User 2 not found"));

        mockMvc.perform(post("/api/users/2/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user to update not found")
    void updateProfile_userNotFound() throws Exception {
        ChangeProfileRequest request = new ChangeProfileRequest("newUsername", "newemail@test.com", "USD", "ENGLISH");

        when(userService.updateProfile(anyLong(), anyString(), anyString(), anyString(), anyString())).thenThrow(new ResourceNotFoundException("User 1 not found"));

        mockMvc.perform(post("/api/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }
}