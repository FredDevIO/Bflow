package io.github.lucyfred.bflow.dto;

public record AdminUserResponseDto(Long id, String username, String email, String currency, String language, String role) {
}
