package io.github.lucyfred.bflow.controller;

import io.github.lucyfred.bflow.config.SecurityConfig;
import io.github.lucyfred.bflow.dto.AuthRequest;
import io.github.lucyfred.bflow.dto.AuthResponse;
import io.github.lucyfred.bflow.dto.UserRequestDto;
import io.github.lucyfred.bflow.dto.UserResponseDto;
import io.github.lucyfred.bflow.exception.DuplicateResourceException;
import io.github.lucyfred.bflow.repository.UserRepository;
import io.github.lucyfred.bflow.security.JwtService;
import io.github.lucyfred.bflow.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/login - Should return 200 and token when credentials are valid")
    void loginWithValidCredentials() throws Exception {
        AuthRequest request = new AuthRequest("Jhon", "123456");
        AuthResponse response = new AuthResponse("emulated-token");

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("emulated-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 when credentials are invalid")
    void loginWithInvalidCredentials() throws Exception {
        AuthRequest request = new AuthRequest("Jhon", "wrong-password");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("POST /api/auth/register - Should return 200 and token when credentials are valid")
    void registerWithValidCredentials() throws Exception {
        UserRequestDto request = new UserRequestDto("JhonEwalds", "jhon@gmail.com", "12345678");
        AuthResponse response = new AuthResponse("emulated-token");

        when(authService.signUp(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("emulated-token"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 when username is already in use")
    void registerWithExistingUsername() throws Exception {
        UserRequestDto request = new UserRequestDto("JhonEwalds", "jhon@gmail.com", "12345678");

        when(authService.signUp(any())).thenThrow(new DuplicateResourceException("Username is already in use"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.messages[0]").value("Username is already in use"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 when email is already in use")
    void registerWithExistingEmail() throws Exception {
        UserRequestDto request = new UserRequestDto("JhonEwalds", "jhon@gmail.com", "12345678");

        when(authService.signUp(any())).thenThrow(new DuplicateResourceException("Email is already in use"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.messages[0]").value("Email is already in use"));
    }

}
