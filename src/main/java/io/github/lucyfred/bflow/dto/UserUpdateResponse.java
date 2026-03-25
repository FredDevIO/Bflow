package io.github.lucyfred.bflow.dto;

public record UserUpdateResponse(
        UserResponseDto user,
        String token
) {
}
