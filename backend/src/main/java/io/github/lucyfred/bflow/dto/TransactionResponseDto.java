package io.github.lucyfred.bflow.dto;

import io.github.lucyfred.bflow.enums.CategoryTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDto(
        Long id,
        BigDecimal amount,
        String description,
        CategoryTypes categoryType,
        String categoryName,
        LocalDate transactionDate
) {
}
