package io.github.lucyfred.bflow.dto;

import io.github.lucyfred.bflow.enums.CategoryTypes;

public record CategoryResponseDto(
        Long id,
        String name,
        CategoryTypes type,
        String color
) {}
