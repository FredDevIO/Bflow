package io.github.lucyfred.bflow.dto;

import io.github.lucyfred.bflow.enums.CategoryTypes;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
        @Size(min = 6, max = 50, message = "Name must contain at least between 6 and 50 characters")
        @NotBlank(message = "Name cannot be empty")
        String name,

        @Enumerated(EnumType.STRING)
        @NotNull(message = "Category cannot be empty")
        CategoryTypes type,

        @Size(min = 7, max = 7, message = "Color must contain 7 characters")
        @NotBlank(message = "Color cannot be empty")
        String color
) {}
