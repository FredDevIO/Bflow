package io.github.lucyfred.bflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
public record UserRequestDto(
    @Size(min = 8, max = 50, message = "Username must contain at least between 8 and 50 characters")
    @NotBlank(message = "Username cannot be empty")
    String username,

    @Email(message = "Email must be email format")
    @NotBlank(message = "Email cannot be empty")
    String email,

    @Size(min = 8, message = "Password must contain at least 8 characters")
    @NotBlank(message = "Password cannot be empty")
    String password
) {}
