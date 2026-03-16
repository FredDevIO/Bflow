package io.github.lucyfred.bflow.service.impl;

import io.github.lucyfred.bflow.dto.AuthResponse;
import io.github.lucyfred.bflow.security.JwtService;
import io.github.lucyfred.bflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails user = userDetailsService.loadUserByUsername(username);

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken);
    }
}
