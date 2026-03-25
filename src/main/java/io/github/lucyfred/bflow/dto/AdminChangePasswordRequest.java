package io.github.lucyfred.bflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminChangePasswordRequest(
        @NotNull(message = "New password cannot be empty")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String newPassword
) {
}
