package io.github.lucyfred.bflow.dto;

public record ChangeProfileRequest(
        String newUsername,
        String newEmail,
        String currency,
        String language
) {
}
